package org.dainst.gazetteer.converter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Comment;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.GroupInternalData;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.dainst.gazetteer.domain.User;

@Component
public class JsonPlaceDeserializer {
	
	private final static Logger logger = LoggerFactory.getLogger("org.dainst.gazetteer.JsonPlaceDeserializer");
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Autowired
	private PlaceRepository placeDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	public Place deserializeLazily(InputStream jsonStream) throws InvalidIdException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode objectNode = mapper.readValue(jsonStream, ObjectNode.class);
			
			Place place = null;
			if (objectNode.has("@id")) {
				place = getPlaceForNode(objectNode.get("@id"));
			}
			return place;
		} catch (Exception e) {
			throw new InvalidIdException("error while getting id from json", e);
		}
	}

	public Place deserialize(InputStream jsonStream) throws HttpMessageNotReadableException {
		
		try {
		
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode objectNode = mapper.readValue(jsonStream, ObjectNode.class);
			
			Place place = null;
			if (objectNode.has("@id")) {
				place = getPlaceForNode(objectNode.get("@id"));
			}
			
			if (place == null) {
				place = new Place();
				if (objectNode.has("@id")) {
					String placeIdString = objectNode.get("@id").asText().replace(baseUri + "place/", "");
					logger.debug("read id from uri: {}", placeIdString);
					place.setId(placeIdString);
				} 
				/*else {
					place = placeDao.save(place);
					logger.debug("created new place with id: {}", place.getId());
				}*/
			}
			
			// set parent place from URI 
			place.setParent(null);
			JsonNode parentNode = objectNode.get("parent");
			if (parentNode != null) {
				Place parent = getPlaceForNode(parentNode);
				if (parent != null && place.getParent() != parent.getId()) {
					if (place.getParent() != null) {
						Place oldParent = placeDao.findById(place.getParent()).orElse(null);
						oldParent.setChildren(oldParent.getChildren()-1);
						placeDao.save(oldParent);
					}
					place.setParent(parent.getId());
					parent.setChildren(parent.getChildren()+1);
					placeDao.save(parent);
				}
			}
			
			// set ancestors
			if (objectNode.has("ancestors")) {
				List<String> ancestors = new ArrayList<String>();
				for (JsonNode ancestorId : objectNode.get("ancestors"))
					ancestors.add(ancestorId.asText().replace(baseUri + "place/", ""));
				place.setAncestors(ancestors);
			}
			
			// set related places from URIs
			JsonNode relatedPlacesNode = objectNode.get("relatedPlaces");
			place.setRelatedPlaces(new HashSet<String>());
			if (relatedPlacesNode != null) for (JsonNode relatedPlaceNode : relatedPlacesNode) {
				Place relatedPlace = getPlaceForNode(relatedPlaceNode);
				if (relatedPlace != null)
					place.addRelatedPlace(relatedPlace.getId());
			}		
			
			// update types
			if (objectNode.has("types")) {
				Set<String> types = new HashSet<String>();
				for (JsonNode type : objectNode.get("types"))
					types.add(type.asText());
				place.setTypes(types);
			}
			
			// update name objects
			JsonNode prefNameNode = objectNode.get("prefName");
			if(prefNameNode != null) {
				PlaceName prefName = new PlaceName();
				JsonNode languageNode = prefNameNode.get("language"); 
				JsonNode titleNode = prefNameNode.get("title");
				JsonNode ancientNode = prefNameNode.get("ancient");
				JsonNode transliteratedNode = prefNameNode.get("transliterated");
				if (titleNode == null)
					throw new HttpMessageNotReadableException("Invalid prefName object. Attribute \"title\" has to be set.");
				if (languageNode != null) prefName.setLanguage(languageNode.asText());
				if (ancientNode != null) prefName.setAncient(ancientNode.asBoolean());
				if (transliteratedNode != null) prefName.setTransliterated(transliteratedNode.asBoolean());
				prefName.setTitle(titleNode.asText());
				logger.debug("updated placename: {}", prefName);
				place.setPrefName(prefName);
			}			
			Set<PlaceName> names = new HashSet<PlaceName>();
			JsonNode namesNode = objectNode.get("names");
			if (namesNode != null) for (JsonNode nameNode : namesNode) {
				PlaceName name = new PlaceName();
				names.add(name);				
				JsonNode languageNode = nameNode.get("language"); 
				JsonNode titleNode = nameNode.get("title");
				JsonNode ancientNode = nameNode.get("ancient");
				JsonNode transliteratedNode = nameNode.get("transliterated");
				if (titleNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"title\" has to be set.");
				if (languageNode != null) name.setLanguage(languageNode.asText());
				if (ancientNode != null) name.setAncient(ancientNode.asBoolean());
				if (transliteratedNode != null) name.setTransliterated(transliteratedNode.asBoolean());
				name.setTitle(titleNode.asText());
				logger.debug("updated placename: {}", name);
			}
			place.setNames(names);
			
			// update location objects
			JsonNode prefLocationNode = objectNode.get("prefLocation");
			if (prefLocationNode != null) {
				Location prefLocation = new Location();
				JsonNode coordinatesNode = prefLocationNode.get("coordinates");
				if (coordinatesNode != null && coordinatesNode.size() > 0) {					
					JsonNode longNode = coordinatesNode.get(0);
					if (longNode == null)
						throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
					JsonNode latNode = coordinatesNode.get(1);
					if (latNode == null)
						throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");					
		
					double lng = longNode.asDouble(1000);
					double lat = latNode.asDouble(1000);					
					if (lat > 90 || lat < -90 || lng > 180 || lng < -180)
						throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
					
					prefLocation.setCoordinates(new double[]{lng, lat});
					
					if (prefLocationNode.has("confidence"))
						prefLocation.setConfidence(prefLocationNode.get("confidence").asInt());
				}
				
				if (prefLocationNode.get("altitude") != null && !prefLocationNode.get("altitude").asText().isEmpty()) {
					String altitude = prefLocationNode.get("altitude").asText();
					altitude = altitude.replace(",", ".");
					prefLocation.setAltitude(Double.valueOf(altitude));
				}
				else
					prefLocation.setAltitude(null);
				
				if (prefLocationNode.has("shape")) {
					JsonNode shapeNode = prefLocationNode.get("shape");
					if (shapeNode.size() > 0) {
						double[][][][] shapeCoordinates = new double[shapeNode.size()][][][];
						for (int i = 0; i < shapeNode.size(); i++) {
							shapeCoordinates[i] = new double[shapeNode.get(i).size()][][];
							for (int j = 0; j < shapeNode.get(i).size(); j++) {
								shapeCoordinates[i][j] = new double[shapeNode.get(i).get(j).size()][];
								for (int k = 0; k < shapeNode.get(i).get(j).size(); k++) {
									shapeCoordinates[i][j][k] = new double[shapeNode.get(i).get(j).get(k).size()];
									for (int l = 0; l < shapeNode.get(i).get(j).get(k).size(); l++) {
										shapeCoordinates[i][j][k][l] = shapeNode.get(i).get(j).get(k).get(l).asDouble();
									}
								}
							}
						}
						Shape shape = new Shape();
						shape.setCoordinates(shapeCoordinates);
						prefLocation.setShape(shape);
					}
				} else
					prefLocation.setShape(null);
				
				if (prefLocation.getCoordinates() != null || prefLocation.getShape() != null) {
					if (prefLocationNode.has("publicSite"))
						prefLocation.setPublicSite(prefLocationNode.get("publicSite").asBoolean());
					else if (place.getTypes().contains("archaeological-site"))
						prefLocation.setPublicSite(false);
					
					place.setPrefLocation(prefLocation);	
					logger.debug("updated location: {}", prefLocation);
				} else
					place.setPrefLocation(null);
			} else
				place.setPrefLocation(null);
			
			Set<Location> locations = new HashSet<Location>();
			JsonNode locationsNode = objectNode.get("locations");
			if (locationsNode != null) for (JsonNode locationNode : locationsNode) {
				Location location = new Location();					
				JsonNode coordinatesNode = locationNode.get("coordinates");
				if (coordinatesNode != null && coordinatesNode.size() > 0) {
					JsonNode longNode = coordinatesNode.get(0);
					if (longNode == null)
						throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
					JsonNode latNode = coordinatesNode.get(1);
					if (latNode == null)
						throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");					
	
					double lng = longNode.asDouble(1000);
					double lat = latNode.asDouble(1000);					
					if (lat > 90 || lat < -90 || lng > 180 || lng < -180)
						throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
				
					location.setCoordinates(new double[]{lng, lat});
				
					if (locationNode.has("confidence"))
						location.setConfidence(locationNode.get("confidence").asInt());
				}
				
				if (locationNode.get("altitude") != null && !locationNode.get("altitude").asText().isEmpty()) {
					String altitude = locationNode.get("altitude").asText();
					altitude = altitude.replace(",", ".");
					location.setAltitude(Double.valueOf(altitude));
				}
				else
					location.setAltitude(null);
				
				if (locationNode.has("shape")) {
					JsonNode shapeNode = locationNode.get("shape");
					double[][][][] shapeCoordinates = new double[shapeNode.size()][][][];
					for (int i = 0; i < shapeNode.size(); i++) {
						shapeCoordinates[i] = new double[shapeNode.get(i).size()][][];
						for (int j = 0; j < shapeNode.get(i).size(); j++) {
							shapeCoordinates[i][j] = new double[shapeNode.get(i).get(j).size()][];
							for (int k = 0; k < shapeNode.get(i).get(j).size(); k++) {
								shapeCoordinates[i][j][k] = new double[shapeNode.get(i).get(j).get(k).size()];
								for (int l = 0; l < shapeNode.get(i).get(j).get(k).size(); l++) {
									shapeCoordinates[i][j][k][l] = shapeNode.get(i).get(j).get(k).get(l).asDouble();
								}
							}
						}
					}
					Shape shape = new Shape();
					shape.setCoordinates(shapeCoordinates);
					location.setShape(shape);
				}
				if (locationNode.has("publicSite"))
					location.setPublicSite(locationNode.get("publicSite").asBoolean());
				else if (place.getTypes().contains("archaeological-site"))
					location.setPublicSite(false);
				
				locations.add(location);				
				logger.debug("updated location: {}", location);
				
			}
			place.setLocations(locations);
			
			if (objectNode.get("unlocatable") != null && objectNode.get("unlocatable").asBoolean())
				place.setUnlocatable(true);
			else
				place.setUnlocatable(false);
			
			// update comment objects			
			List<Comment> comments = new ArrayList<Comment>();
			JsonNode commentsNode = objectNode.get("comments");
			if (commentsNode != null) for (JsonNode commentNode : commentsNode) {
				Comment comment = new Comment();					
				comments.add(comment);
				JsonNode languageNode = commentNode.get("language"); 
				JsonNode textNode = commentNode.get("text");
				if (textNode == null)
					throw new HttpMessageNotReadableException("Invalid comment object. Attribute \"text\" has to be set.");
				if (languageNode != null) comment.setLanguage(languageNode.asText());
				comment.setText(textNode.asText());
				logger.debug("updated comment: {}", comment);				
			}
			place.setComments(comments);
			
			// update tags	
			Set<String> tags = new HashSet<String>();
			JsonNode tagsNode = objectNode.get("tags");
			if (tagsNode != null) for (JsonNode tagNode : tagsNode) {
				tags.add(tagNode.asText());	
			}
			logger.debug("updated tags: {}", tags);	
			place.setTags(tags);
			
			// update provenance	
			Set<String> provenance = new HashSet<String>();
			JsonNode provenanceNode = objectNode.get("provenance");
			if (provenanceNode != null) for (JsonNode provenanceEntryNode : provenanceNode) {				
				provenance.add(provenanceEntryNode.asText());	
			}
			logger.debug("updated provenance: {}", provenance);
			place.setProvenance(provenance);
			
			// update record group
			if (objectNode.get("recordGroup") != null) {
				JsonNode recordGroupNode = objectNode.get("recordGroup");
				if (recordGroupNode.get("id") != null) {
					String recordGroupId = recordGroupNode.get("id").asText();
					if (recordGroupId.length() > 0)
						place.setRecordGroupId(recordGroupId);
					else
						place.setRecordGroupId(null);
				}				
			}
			
			// update identifier objects			
			Set<Identifier> identifiers = new HashSet<Identifier>();
			JsonNode identifiersNode = objectNode.get("identifiers");
			if (identifiersNode != null) for (JsonNode identifierNode : identifiersNode) {
				Identifier identifier = new Identifier();					
				identifiers.add(identifier);
				JsonNode valueNode = identifierNode.get("value"); 
				JsonNode contextNode = identifierNode.get("context");
				if (valueNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"value\" has to be set.");
				if (contextNode != null) identifier.setContext(contextNode.asText());
				identifier.setValue(valueNode.asText());
				logger.debug("updated identifier: {}", identifier);				
			}
			place.setIdentifiers(identifiers);
			
			// update link objects			
			Set<Link> links = new HashSet<Link>();
			JsonNode linksNode = objectNode.get("links");
			if (linksNode != null) for (JsonNode linkNode : linksNode) {
				Link link = new Link();					
				links.add(link);
				JsonNode objNode = linkNode.get("object"); 
				JsonNode predicateNode = linkNode.get("predicate");	
				JsonNode descriptionNode = linkNode.get("description");
				if (objNode == null)
					throw new HttpMessageNotReadableException("Invalid link object. Attribute \"object\" has to be set.");
				if (predicateNode == null)
					throw new HttpMessageNotReadableException("Invalid link object. Attribute \"predicate\" has to be set.");
				link.setObject(objNode.asText());
				link.setPredicate(predicateNode.asText());
				if (descriptionNode != null && !descriptionNode.asText().equals(""))
					link.setDescription(descriptionNode.asText());
				else
					link.setDescription(null);
				logger.debug("updated link: {}", link);				
			}
			place.setLinks(links);
			
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.debug("user: {}", principal);
			if (principal instanceof User) {
				User user = (User) principal;
				
				// update reisestipendium content
				if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_REISESTIPENDIUM"))) {
					if (objectNode.has("noteReisestipendium"))
						place.setNoteReisestipendium(objectNode.get("noteReisestipendium").asText());
					List<Comment> commentsReisestipendium = new ArrayList<Comment>();
					JsonNode commentsReisestipendiumNode = objectNode.get("commentsReisestipendium");
					if (commentsReisestipendiumNode != null) for (JsonNode commentNode : commentsReisestipendiumNode) {
						Comment comment = new Comment();					
						commentsReisestipendium.add(comment);
						JsonNode userNode = commentNode.get("user"); 
						JsonNode textNode = commentNode.get("text");
						if (textNode == null)
							throw new HttpMessageNotReadableException("Invalid comment object. Attribute \"text\" has to be set.");
						if (userNode != null) {
							if (!userNode.isNull()) comment.setUser(userNode.asText());
						} else {
							comment.setUser(user.getUsername());
						}
						comment.setText(textNode.asText());
						logger.debug("updated comment: {}", comment);
					}
					place.setCommentsReisestipendium(commentsReisestipendium);
				}
				
				// update record group internal data
				List<GroupInternalData> groupInternalData = new ArrayList<GroupInternalData>();
				for (GroupInternalData data : place.getGroupInternalData()) {
					GroupRole role = groupRoleDao.findByGroupIdAndUserId(data.getGroupId(), user.getId());
					if (role == null)
						groupInternalData.add(data);
				}
				JsonNode groupInternalDataNode = objectNode.get("groupInternalData");
				if (groupInternalDataNode != null) for (JsonNode dataNode : groupInternalDataNode) {
					GroupInternalData data = new GroupInternalData();					
					JsonNode textNode = dataNode.get("text");
					JsonNode groupNode = dataNode.get("recordGroup");
					if (textNode == null)
						throw new HttpMessageNotReadableException(
								"Invalid group internal data object. Attribute \"text\" has to be set.");
					if (groupNode == null)
						throw new HttpMessageNotReadableException(
								"Invalid group internal data object. Attribute \"recordGroup\" has to be set.");
										
					data.setText(textNode.asText());
					data.setGroupId(groupNode.get("id").asText());
					
					GroupRole role = groupRoleDao.findByGroupIdAndUserId(data.getGroupId(), user.getId());
					if (role != null) {
						groupInternalData.add(data);
						logger.debug("updated group internal data: {}", data);
					}
				}
				place.setGroupInternalData(groupInternalData);
			}
			
			logger.debug("returning place {}", place);
			
			return place;
			
		} catch (Exception e) {
			String msg = "Unable to deserialize json to place object";
			logger.error(msg, e);
			throw new HttpMessageNotReadableException(e.getMessage(), e);
		}
		
	}
	
	private Place getPlaceForNode(JsonNode node) throws InvalidIdException {
		
		String placeUri = node.asText();
		
		if (placeUri.startsWith(baseUri)) {
			String placeIdString = placeUri.replace(baseUri + "place/", "");
			try {
				Place place = placeDao.findById(placeIdString).orElse(null);
				return place;
			} catch (NumberFormatException e) {
				throw new InvalidIdException("Invalid id: " + placeIdString, e);
			}
		} else {
			return placeDao.getByLinksObjectAndLinksPredicate(placeUri, "owl:sameAs");
		}
		
	}
	
	private static class InvalidIdException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public InvalidIdException(String msg, Throwable cause) {
			super(msg, cause);
		}
		
	}
	
}
