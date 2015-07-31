package org.dainst.gazetteer.controller;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.helpers.GrandparentsHelper;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.Merger;
import org.dainst.gazetteer.helpers.PlaceAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MergeController {

	private static final Logger logger = LoggerFactory.getLogger(MergeController.class);
	
	@Autowired
	private PlaceRepository placeDao;
	
	@Autowired
	private PlaceChangeRecordRepository changeRecordDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	private Merger merger;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Value("${baseUri}")
	private String baseUri;

	@RequestMapping(value="/merge/{id1}/{id2}", method=RequestMethod.POST)
	public ModelAndView getPlace(@PathVariable String id1,
			@PathVariable String id2,
			HttpServletResponse response) {
		
		Place place1 = placeDao.findOne(id1);
		Place place2 = placeDao.findOne(id2);
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupRoleDao);
		
		if (!placeAccessService.checkPlaceAccess(place1, true) || !placeAccessService.checkPlaceAccess(place2, true))
			throw new IllegalStateException("Places may not be merged, as the user doesn't have the permission to edit both places.");
		
		if (!(place1.getRecordGroupId() == null && place2.getRecordGroupId() == null) && (place1.getRecordGroupId() != null && !place1.getRecordGroupId().equals(place2.getRecordGroupId())))
			throw new IllegalStateException("Places may not be merged, as they belong to different record groups.");
		
		// merge places
		Place newPlace = merger.merge(place1, place2);
		newPlace.setId(idGenerator.generate(newPlace));

		// update grandparent ids
		GrandparentsHelper helper = new GrandparentsHelper(placeDao);
		newPlace.setGrandparents(helper.findGrandparentIds(newPlace));

		Place existingPlace = placeDao.findOne(newPlace.getId());
		if (existingPlace == null) {
			placeDao.save(newPlace);
		} else {
			throw new IllegalStateException("Could not merge places! Creation of place failed because generated ID already exists: " + newPlace.getId());
		}
		
		Set<Place> updatedPlaces = new HashSet<Place>();
		try {
			// update IDs in related places
			for (String relatedPlaceId : newPlace.getRelatedPlaces()) {
				Place relatedPlace = placeDao.findOne(relatedPlaceId);
				if (relatedPlace != null && relatedPlace.getRelatedPlaces() != null) {
					relatedPlace.getRelatedPlaces().remove(id1);
					relatedPlace.getRelatedPlaces().remove(id2);
					relatedPlace.getRelatedPlaces().add(newPlace.getId());
					updatedPlaces.add(relatedPlace);
				}
			}
		
			// update IDs in children
			List<Place> children = placeDao.findByParent(id1);
			for (Place child : children) {
				child.setParent(newPlace.getId());
				updatedPlaces.add(child);
			}		
			children = placeDao.findByParent(id2);
			for (Place child : children) {
				child.setParent(newPlace.getId());
				updatedPlaces.add(child);
			}
		} catch (Exception e) {
			placeDao.delete(newPlace);			
			throw new RuntimeException("Could not merge places: Failed to update related places / children.", e);
		}
		
		for (Place place : updatedPlaces) {
			placeDao.save(place);
		}
		
		if (place1.getChildren() + place2.getChildren() < 10000)
			helper.updatePlaceGrandparents(newPlace);

		place1.setReplacedBy(newPlace.getId());
		place1.setDeleted(true);
		placeDao.save(place1);
		place2.setReplacedBy(newPlace.getId());
		place2.setDeleted(true);
		placeDao.save(place2);

		changeRecordDao.save(createChangeRecord(place1, "replace"));
		changeRecordDao.save(createChangeRecord(place2, "replace"));		
		changeRecordDao.save(createChangeRecord(newPlace, "merge"));
	
		logger.debug("finished merging " + place1.getId() + " and " + place2.getId() + " to " + newPlace.getId());
		
		response.setStatus(201);
		response.setHeader("location", baseUri + "place/" + newPlace.getId());
				
		ModelAndView mav = new ModelAndView("place/get");
		mav.addObject("place", newPlace);
		mav.addObject("baseUri", baseUri);
		mav.addObject("readAccess", true);
		mav.addObject("editAccess", true);
		mav.addObject("accessGranted", placeAccessService.checkPlaceAccess(newPlace));
		return mav;
		
	}
	
	private PlaceChangeRecord createChangeRecord(Place place, String changeType) {
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		PlaceChangeRecord changeRecord = new PlaceChangeRecord();
		changeRecord.setUserId(user.getId());
		changeRecord.setPlaceId(place.getId());
		changeRecord.setChangeType(changeType);
		changeRecord.setChangeDate(new Date());
		
		return changeRecord;
	}
}
