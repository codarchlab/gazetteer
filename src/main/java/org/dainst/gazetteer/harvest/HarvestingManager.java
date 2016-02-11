package org.dainst.gazetteer.harvest;

import org.dainst.gazetteer.dao.HarvesterDefinitionRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.Merger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class HarvestingManager {
	
	private static Logger logger = LoggerFactory.getLogger(HarvestingManager.class);
	
	@Autowired
	private HarvesterDefinitionRepository harvesterDefinitionDao;
	
	@Autowired
	private PlaceRepository placeDao;

	@Autowired
	private TaskScheduler taskScheduler;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Autowired
	private Merger merger;

	public void initialize() {
		logger.info("initializing HarvestingManager");
		Iterable<HarvesterDefinition> defs = harvesterDefinitionDao.findAll();
		for (HarvesterDefinition def : defs) {
			logger.info("scheduling harvesting handler for definition: " + def.getName());
			CronTrigger trigger = new CronTrigger(def.getCronExpression());
			HarvestingHandler handler = new HarvestingHandler(def, placeDao,
					harvesterDefinitionDao, idGenerator, merger);
			taskScheduler.schedule(handler, trigger);
		}
	}

}
