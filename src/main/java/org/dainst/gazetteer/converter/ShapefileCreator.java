package org.dainst.gazetteer.converter;

import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.helpers.PlaceAccessService;
import org.dainst.gazetteer.helpers.PlaceAccessService.AccessStatus;
import org.dainst.gazetteer.helpers.ProtectLocationsService;
import org.dainst.gazetteer.helpers.TempFolderService;
import org.dainst.gazetteer.helpers.ZipArchiveBuilder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@Component
public class ShapefileCreator {

	private final static Logger logger = LoggerFactory.getLogger("org.dainst.gazetteer.converter.ShapefileCreator");

	@Autowired
	private TempFolderService tempFolderHelper;

	@Autowired
	private PlaceRepository placeRepository;

	@Autowired
	private RecordGroupRepository recordGroupRepository;

	@Autowired
	private GroupRoleRepository groupRoleRepository;

	@Autowired
	private ProtectLocationsService protectLocationsService;

	private static final String dataSchema =
				"gazid:String,"
			+   "pnametitle:String,"
			+   "pnamelang:String,"
			+   "pnameanc:Boolean,"
			+   "pnametrnsl:Boolean,"
			+   "name1title:String,"
			+   "name1lang:String,"
			+   "name1anc:Boolean,"
			+   "name1trnsl:Boolean,"
			+   "name2title:String,"
			+   "name2lang:String,"
			+   "name2anc:Boolean,"
			+   "name2trnsl:Boolean,"
			+   "name3title:String,"
			+   "name3lang:String,"
			+   "name3anc:Boolean,"
			+   "name3trnsl:Boolean,"
			+   "name4title:String,"
			+   "name4lang:String,"
			+   "name4anc:Boolean,"
			+   "name4trnsl:Boolean,"
			+   "name5title:String,"
			+   "name5lang:String,"
			+   "name5anc:Boolean,"
			+   "name5trnsl:Boolean,"
			+	"type1:String,"
			+	"type2:String,"
			+	"type3:String,"
			+	"loctype:String,"
			+	"locconfid:Integer,"
			+	"publicsite:Boolean,"
			+	"altitude:Double,"
			+	"parent:Integer,"
			+	"related1:String,"
			+	"related2:String,"
			+	"related3:String,"
			+	"related4:String,"
			+	"related5:String,"
			+	"related6:String,"
			+	"related7:String,"
			+	"related8:String,"
			+	"related9:String,"
			+	"related10:String,"
			+	"tag1:String,"
			+	"tag2:String,"
			+	"tag3:String,"
			+	"tag4:String,"
			+	"tag5:String,"
			+	"prov1:String,"
			+	"prov2:String,"
			+	"prov3:String,"
			+	"prov4:String,"
			+	"prov5:String,"
			+	"id1contxt:String,"
			+	"id1value:String,"
			+	"id2contxt:String,"
			+	"id2value:String,"
			+	"id3contxt:String,"
			+	"id3value:String,"
			+	"id4contxt:String,"
			+	"id4value:String,"
			+	"id5contxt:String,"
			+	"id5value:String,"
			+	"id6contxt:String,"
			+	"id6value:String,"
			+	"id7contxt:String,"
			+	"id7value:String,"
			+	"id8contxt:String,"
			+	"id8value:String,"
			+	"id9contxt:String,"
			+	"id9value:String,"
			+	"id10contxt:String,"
			+	"id10value:String,"
			+	"link1:String,"
			+	"link2:String,"
			+	"link3:String,"
			+	"link4:String,"
			+	"link5:String,"
			+	"comment1:String,"
			+	"comment2:String,"
			+	"comment3:String,"
			+	"recgroup:String,"
			+	"changedate:Date";

	public enum GeometryType {
		POINT,
		MULTIPOLYGON
	}

	public File createShapefile(String filename, String placeId) throws Exception {

		List<String> placeIds = new ArrayList<String>();
		placeIds.add(placeId);

		return createShapefile(filename, placeIds, placeIds, placeId);
	}

	public File createShapefile(String filename, List<String> pointPlaceIds, List<String> multipolygonPlaceIds, String query) throws Exception {

		logger.debug("Create shapefiles for " + pointPlaceIds.size() + " places with points and " + multipolygonPlaceIds.size() + " places with multipolygons");

		File tempFolder = tempFolderHelper.createFolder();
		File shapeFileFolder = new File(tempFolder.getAbsolutePath() + File.separator + filename);
		shapeFileFolder.mkdir();

		File zipFile;

		try {
			if (pointPlaceIds.size() > 0) {
				createFiles(pointPlaceIds, shapeFileFolder, GeometryType.POINT);
			} else {
				logger.debug("No features of feature type point");
			}
			if (multipolygonPlaceIds.size() > 0) {
				createFiles(multipolygonPlaceIds, shapeFileFolder, GeometryType.MULTIPOLYGON);
			} else {
				logger.debug("No features of feature type multipolygon");
			}
			createReadmeFile(shapeFileFolder, query);
			zipFile = ZipArchiveBuilder.buildZipArchiveFromFolder(shapeFileFolder, tempFolder);
		} catch(Exception e) {
			FileUtils.deleteDirectory(tempFolder);
			throw new Exception("Error during shapefile creation. Deleted temp directory.", e);
		}

		return zipFile;
	}

	public void createReadmeFile(File folder, String query) throws Exception {
		String outputFilePath = folder.getAbsolutePath() + File.separator + "README.txt";
		File outputFile = new File(outputFilePath);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		try {
			PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
			writer.println("IDAI.WELT GAZETTEER DATA -- README");
			writer.println("");
			writer.println("Data provider:");
			writer.println("iDAI.welt online services");
			writer.println("");
			writer.println("Original download source:");
			writer.println("https://gazetteer.dainst.org");
			writer.println("");
			writer.println("Date of download:");
			writer.println(dateFormat.format(date));
			writer.println("");
			writer.println("Query string:");
			writer.println(query);
			writer.println("");
			writer.println("Copyright:");
			writer.println("Deutsches Archäologisches Institut (DAI)/German Archaeological Institute");
			writer.println("Please read section 2 (LICENSE) for details.");
			writer.println("");
			writer.println("");
			writer.println("1 DATA DESCRIPTION");
			writer.println("");
			writer.println("This data has been downloaded from the iDAI.welt Gazetteer.");
			writer.println("");
			writer.println("The iDAI.welt Gazetteer is a hierarchical geographic register of places");
			writer.println("(both current and past).");
			writer.println("");
			writer.println("This download contains the results of a user-defined data query in the");
			writer.println("form of GIS objects (features). All GIS objects use geodetic coordinates");
			writer.println("(degrees latitude and longitude). The EPSG code is 4326 (WGS 84 lat/lon).");
			writer.println("");
			writer.println("");
			writer.println("1.1 Spatial precision and accuracy");
			writer.println("");
			writer.println("Your downloaded data may not have full spatial precision.");
			writer.println("");
			writer.println("If the data was downloaded anonymously, an _intentional_ truncation");
			writer.println("of coordinates to just three decimal digits will have been applied.");
			writer.println("The full coordinate precision is six decimal digits and is only provided");
			writer.println("if the user was logged into the Gazetteer website using a registered");
			writer.println("account at the time of downloading.");
			writer.println("");
			writer.println("Note that some sensitive project data may require you to obtain additional");
			writer.println("permission before access to precise data is granted. If in doubt, please");
			writer.println("contact the DAI at one of the addresses listed under 'CONTACT' in this");
			writer.println("document.");
			writer.println("");
			writer.println("Spatial accuracy (independent of chosen coordinate precision) is determined");
			writer.println("by the level of confidence in the original information source. The latter");
			writer.println("is encoded as a numeric value in the 'confidence' attribute field. Please");
			writer.println("see section 1.3 (Attribute data) for details.");
			writer.println("");
			writer.println("");
			writer.println("1.2 File format");
			writer.println("");
			writer.println("This data is delivered in ESRI(tm) Shapefile format. Shapefiles are a very");
			writer.println("common format for GIS vector data that is understood by practically every");
			writer.println("GIS in use today.");
			writer.println("");
			writer.println("The full format specification can be found in this ESRI(tm) white paper:");
			writer.println("");
			writer.println(" https://www.esri.com/library/whitepapers/pdfs/shapefile.pdf");
			writer.println("");
			writer.println("A Shapefile is actually a set of at least three files with a common base");
			writer.println("name and different extensions:");
			writer.println("");
			writer.println(" .shp - geometric object representations");
			writer.println(" .dbf - attribute data as a DBase table");
			writer.println(" .shx - an index that links each geometry to one attribute record");
			writer.println("");
			writer.println("In addition, there is commonly:");
			writer.println("");
			writer.println(" .prj - spatial reference system information");
			writer.println("");
			writer.println("The DAI Gazetteer contains objects of different geometric representations");
			writer.println("(points, polygons). Since each Shapefile can only store one geometry type,");
			writer.println("your downloaded data may contain more than one Shapefile.");
			writer.println(" ");
			writer.println("");
			writer.println("1.3 Attribute data");
			writer.println("");
			writer.println("The iDAI.welt Gazetteer stores rich attribute data.");
			writer.println("Shapefiles use attribute tables in DBase format which places some restrictions");
			writer.println("on the types and length of data. Most significantly, string fields can hold a");
			writer.println("maximum of 254 characters; text will be truncated in case of longer entries.");
			writer.println("");
			writer.println("In addition, the number of attribute fields is fixed for Shapefile export.");
			writer.println("In some rare cases, the original Gazetteer data may have further fields for");
			writer.println("e.g. localized place names that will be dropped from the export.");
			writer.println("");
			writer.println("All text attribute data use UTF-8 encoding.");
			writer.println("");
			writer.println("The following is a list of attribute fields that are preserved in the");
			writer.println("Shapefile export.");
			writer.println("");
			writer.println(" Name		Type	Description");
			writer.println(" ————————————————");
			writer.println("");
			writer.println(" gazid		string	The unique Gazetteer key for this record.");
			writer.println("");
			writer.println(" pnametitle string	The primary/common name of the place.");
			writer.println("");
			writer.println(" pnamelang  string	Three-letter ISO 639-3 code that denotes the language of");
			writer.println("					the primary place name.");
			writer.println("");
			writer.println(" pnameanc	boolean	???");
			writer.println("");
			writer.println(" pnametrnsl	boolean	???");
			writer.println("");
			writer.println(" nameNtitle,		(With N=1...5): In analogy to the 'pname' attribute listed");
			writer.println(" nameNlang,			above, these are up to five alternative place names.");
			writer.println(" nameNanc,");
			writer.println(" nameNtrnsl");
			writer.println("");
			writer.println(" typeN		string	(With N=1...3): Gazetteer place types:");
			writer.println("					continent");
			writer.println("					island");
			writer.println("					landform");
			writer.println("					hydrography");
			writer.println("					landcover");
			writer.println("					administrative-unit");
			writer.println("					populated-place");
			writer.println("					archaeological-site");
			writer.println("					archaeological-area");
			writer.println("					building-institution");
			writer.println(" ");
			writer.println(" loctype	string	???:");
			writer.println("					preferred location");
			writer.println("					alternative location	");
			writer.println("");
			writer.println(" locconfid 	integer	The level of confidence in the accuracy of locational");
			writer.println("					data (=coordinates):");
			writer.println("					0 = no data");
			writer.println("					1 = inaccurate");
			writer.println("					2 = accurate");
			writer.println("					3 = exact");
			writer.println("					4 = wrong");
			writer.println(" ");
			writer.println(" publicsite	boolean	Whether a site is accessible to the public or not.");
			writer.println("");
			writer.println(" altitude	double	An elevation reading for the place (if available) in metres.");
			writer.println("");
			writer.println(" parent	 	integer	The unique Gazetteer key (if applicable) of a place that is");
			writer.println("				    the parent of (i.e. contains) the current place.");
			writer.println("");
			writer.println(" relatedN	string	(With N=1...10): ???");
			writer.println("");
			writer.println(" tagN		string	(With N=1..5): Associated Gazetteer tags.");
			writer.println("");
			writer.println(" provN		string	(With N=1..5): Proveniance (source) information.");
			writer.println("");
			writer.println(" idNcontxt	string 	(With N=1..10): ???");
			writer.println("");
			writer.println(" idNvalue	string  (With N=1..10): ???");
			writer.println("");
			writer.println(" linkN		string  (With N=1..5): ???");
			writer.println("");
			writer.println(" commentN	string  (With N=1..5): ???");
			writer.println("");
			writer.println(" recgroup	string	???");
			writer.println("");
			writer.println(" changedate	date	Most recent date of record modification.");
			writer.println("");
			writer.println("");
			writer.println("2 LICENSE");
			writer.println("");
			writer.println("Unless otherwise stated, all data in this download is licensed under CC-BY.");
			writer.println("Please review the full license conditions here:");
			writer.println("");
			writer.println(" https://creativecommons.org/licenses/by");
			writer.println("");
			writer.println("");
			writer.println("3 CONTACT");
			writer.println("");
			writer.println("idai.gazetteer@dainst.de");
			writer.close();
		} catch (Exception e) {
			throw new Exception("Error while creating readme file.", e);
		}
	}

	public void removeShapefileData(File shapefile) throws IOException {

		File folder = new File(shapefile.getParent());
		FileUtils.deleteDirectory(folder);
	}

	private void createFiles(List<String> placeIds, File folder, GeometryType geometryType) throws Exception {

		String outputFilePath = folder.getAbsolutePath() + File.separator + geometryType.name().toLowerCase() + "s.shp";
		File outputFile = new File(outputFilePath);

		MemoryDataStore memoryDataStore = createMemoryDataStore(placeIds, geometryType);

		if (memoryDataStore != null) {
			logger.debug("Write shapefile for feature type " + geometryType.name().toLowerCase());
			writeShapefile(memoryDataStore, outputFile);
		}
	}

	private MemoryDataStore createMemoryDataStore(List<String> placeIds, GeometryType geometryType) throws Exception {

		SimpleFeatureType featureType = createFeatureType(geometryType);

		MemoryDataStore memoryDataStore = new MemoryDataStore();
		memoryDataStore.createSchema(featureType);

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

		PlaceAccessService placeAccessService = new PlaceAccessService(recordGroupRepository, groupRoleRepository);

		Transaction transaction = Transaction.AUTO_COMMIT;

		boolean dataStoreEmpty = true;

		for (String placeId : placeIds) {

			Place place = getPlace(placeId, placeAccessService);

			if (containsGeometryOfFeatureType(place.getPrefLocation(), geometryType)) {
				if (addFeature(place, place.getPrefLocation(), "preferred location", featureType, geometryType, geometryFactory, memoryDataStore, transaction)) {
					dataStoreEmpty = false;
				}
			}

			for (Location location : place.getLocations()) {
				if (containsGeometryOfFeatureType(location, geometryType)) {
					if (addFeature(place, location, "alternative location", featureType, geometryType, geometryFactory, memoryDataStore, transaction)) {
						dataStoreEmpty = false;
					}
				}
			}
		}

		transaction.close();
		transaction = null;

		if (!dataStoreEmpty) {
			return memoryDataStore;
		} else {
			return null;
		}
	}

	private SimpleFeatureType createFeatureType(GeometryType geometryType) throws Exception {

		String schema = null;

		switch(geometryType) {
		case POINT:
			schema = "the_geom:Point:srid=4326," + dataSchema;
			break;
		case MULTIPOLYGON:
			schema = "the_geom:MultiPolygon:srid=4326," + dataSchema;
			break;
		}

		try {
			return DataUtilities.createType(geometryType.name().toLowerCase(), schema);
		} catch (SchemaException e) {
			throw new Exception("Failed to create feature types for shapefile export", e);
		}
	}

	private Place getPlace(String id, PlaceAccessService placeAccessService) {

		Place place = placeRepository.findOne(id);

		if (place == null) {
			logger.warn("Place " + id + " not found");
			return null;
		}

		AccessStatus accessStatus = placeAccessService.getAccessStatus(place);

		if (!PlaceAccessService.hasReadAccess(accessStatus)) {
			return null;
		}

		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;

		protectLocationsService.protectLocations(user, place, accessStatus);

		return place;
	}

	private boolean containsGeometryOfFeatureType(Location location, GeometryType geometryType) {

		if (geometryType == GeometryType.POINT) {
			return (location != null && location.getCoordinates() != null && location.getCoordinates().length == 2);
		} else if (geometryType == GeometryType.MULTIPOLYGON) {
			return (location != null && location.getShape() != null && location.getShape().getCoordinates() != null
					&& location.getShape().getCoordinates().length > 0);
		} else {
			return false;
		}
	}

	private boolean addFeature(Place place, Location location, String locationType, SimpleFeatureType featureType, GeometryType geometryType,
			GeometryFactory geometryFactory, MemoryDataStore memoryDataStore, Transaction transaction) throws Exception {

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

		if (geometryType == GeometryType.POINT) {
			featureBuilder.add(createPointGeometry(location, geometryFactory));
		} else if (geometryType == GeometryType.MULTIPOLYGON) {
			MultiPolygon multiPolygon = createMultiPolygonGeometry(location, geometryFactory);
			if (multiPolygon != null) {
				featureBuilder.add(multiPolygon);
			} else {
				return false;
			}
		}

		fillFeatureFields(place, location, locationType, featureBuilder);

		SimpleFeature feature = featureBuilder.buildFeature(null);
		featureBuilder = null;

		memoryDataStore.addFeature(feature);

		try {
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			transaction.close();
			throw new Exception("Could not write feature for place " + place.getId() + " and featureType " + featureType.getTypeName(), e);
		}

		return true;
	}

	private void fillFeatureFields(Place place, Location location, String locationType, SimpleFeatureBuilder featureBuilder) {

		// Gazetteer ID
		featureBuilder.add(Integer.parseInt(place.getId()));

		// Prefered name
		if (place.getPrefName() != null) {
			featureBuilder.add(place.getPrefName().getTitle());
			featureBuilder.add(place.getPrefName().getLanguage());
			featureBuilder.add(place.getPrefName().isAncient());
			featureBuilder.add(place.getPrefName().isTransliterated());
		} else {
			for (int i = 0; i < 2; i++) featureBuilder.add("");
			for (int i = 0; i < 2; i++) featureBuilder.add(false);
		}

		// Alternative names
		if (place.getNames() != null) {
			List<PlaceName> alternativeNames = new ArrayList<PlaceName>(place.getNames());
			for (int i = 0; i < 5; i++) {
				if (i >= alternativeNames.size()) {
					for (int j = 0; j < 2; j++) featureBuilder.add("");
					for (int j = 0; j < 2; j++) featureBuilder.add(false);
				} else {
					featureBuilder.add(alternativeNames.get(i).getTitle());
					featureBuilder.add(alternativeNames.get(i).getLanguage());
					featureBuilder.add(alternativeNames.get(i).isAncient());
					featureBuilder.add(alternativeNames.get(i).isTransliterated());
				}
			}
		} else {
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 2; j++) featureBuilder.add("");
				for (int j = 0; j < 2; j++) featureBuilder.add(false);
			}
		}

		// Types
		addFeatureEntries(featureBuilder, place.getTypes(), 3);

		// Location
		featureBuilder.add(locationType);
		featureBuilder.add(location.getConfidence());
		featureBuilder.add(location.isPublicSite());
		if (location.getAltitude() != null) {
			featureBuilder.add(location.getAltitude());
		} else {
			featureBuilder.add(Double.NaN);
		}

		// Parent
		if (place.getParent() != null) {
			featureBuilder.add(place.getParent());
		} else {
			featureBuilder.add("");
		}

		// Related places
		addFeatureEntries(featureBuilder, place.getRelatedPlaces(), 10);

		// Tags
		addFeatureEntries(featureBuilder, place.getTags(), 5);

		// Provenance tags
		addFeatureEntries(featureBuilder, place.getProvenance(), 5);

		// Identifiers
		if (place.getIdentifiers() != null) {
			List<Identifier> identifiers = new ArrayList<Identifier>(place.getIdentifiers());
			for (int i = 0; i < 10; i++) {
				if (i >= identifiers.size()) {
					for (int j = 0; j < 2; j++) featureBuilder.add("");
				} else {
					featureBuilder.add(identifiers.get(i).getContext());
					featureBuilder.add(identifiers.get(i).getValue());
				}
			}
		} else {
			for (int i = 0; i < 20; i++) featureBuilder.add("");
		}

		// Links
		if (place.getLinks() != null) {
			List<Link> links = new ArrayList<Link>(place.getLinks());
			for (int i = 0; i < 5; i++) {
				if (i >= links.size()) {
					featureBuilder.add("");
				} else {
					featureBuilder.add(links.get(i).getPredicate() + " " + links.get(i).getObject());
				}
			}
		} else {
			for (int i = 0; i < 5; i++) featureBuilder.add("");
		}

		// Comments
		if (place.getComments() != null) {
			for (int i = 0; i < 3; i++) {
				if (i >= place.getComments().size()) {
					featureBuilder.add("");
				} else {
					featureBuilder.add(place.getComments().get(i).getText());
				}
			}
		} else {
			for (int i = 0; i < 3; i++) featureBuilder.add("");
		}

		// Record group
		if (place.getRecordGroupId() != null) {
			RecordGroup recordGroup = recordGroupRepository.findOne(place.getRecordGroupId());
			if (recordGroup != null) {
				featureBuilder.add(recordGroup.getName());
			} else {
				featureBuilder.add("");
			}
		} else {
			featureBuilder.add("");
		}

		// Last change date
		if (place.getLastChangeDate() != null) {
			featureBuilder.add(place.getLastChangeDate());
		} else {
			Calendar calendar = new GregorianCalendar();
			calendar.setTimeInMillis(0);
			featureBuilder.add(calendar.getTime());
		}

	}

	private void addFeatureEntries(SimpleFeatureBuilder featureBuilder, Set<String> set, int size) {

		if (set != null) {
			List<String> entries = new ArrayList<String>(set);
			for (int i = 0; i < size; i++) {
				if (i >= entries.size()) {
					featureBuilder.add("");
				} else {
					featureBuilder.add(entries.get(i));
				}
			}
		} else {
			for (int i = 0; i < size; i++) featureBuilder.add("");
		}
	}

	private void writeShapefile(MemoryDataStore memoryDataStore, File outputFile) throws Exception {

		SimpleFeatureSource featureSource = memoryDataStore.getFeatureSource(memoryDataStore.getTypeNames()[0]);
		SimpleFeatureType featureType = featureSource.getSchema();

		Map<String, java.io.Serializable> creationParams = new HashMap<String, java.io.Serializable>();
		creationParams.put("url", DataUtilities.fileToURL(outputFile));

		ShapefileDataStoreFactory factory = (ShapefileDataStoreFactory) FileDataStoreFinder.getDataStoreFactory("shp");
		ShapefileDataStore dataStore = (ShapefileDataStore) factory.createNewDataStore(creationParams);
		dataStore.setCharset(Charset.forName("UTF-8"));
		dataStore.createSchema(featureType);

		SimpleFeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);

		Transaction transaction = new DefaultTransaction();
		try {
			SimpleFeatureCollection collection = featureSource.getFeatures();
			featureStore.addFeatures(collection);
			transaction.commit();
		} catch (IOException e) {
			try {
				transaction.rollback();
				throw new Exception("Failed to commit data to feature store", e);
			} catch (IOException e2 ) {
				logger.error("Failed to commit data to feature store", e);
				throw new Exception("Transaction rollback failed", e2);
			}
		} finally {
			transaction.close();
		}
	}

	private Point createPointGeometry(Location location, GeometryFactory geometryFactory) {

		return geometryFactory.createPoint(new Coordinate(location.getCoordinates()[0], location.getCoordinates()[1]));
	}

	private MultiPolygon createMultiPolygonGeometry(Location location, GeometryFactory geometryFactory) {

		double[][][][] coordinates = location.getShape().getCoordinates();

		Polygon[] polygons = new Polygon[coordinates.length];

		for (int i = 0; i < coordinates.length; i++) {
			double[][][] polygonCoordinates = coordinates[i];
			LinearRing[] linearRings = new LinearRing[polygonCoordinates.length];

			for (int j = 0; j < polygonCoordinates.length; j++) {
				double[][] linearRingCoordinates = polygonCoordinates[j];
				Coordinate[] points = new Coordinate[linearRingCoordinates.length];

				for (int k = 0; k < linearRingCoordinates.length; k++) {
					double[] pointCoordinates = linearRingCoordinates[k];
					points[k] = new Coordinate(pointCoordinates[0], pointCoordinates[1]);
				}

				linearRings[j] = geometryFactory.createLinearRing(points);
			}

			if (linearRings.length == 0) return null;

			LinearRing shell = linearRings[0];

			if (!shell.isClosed() || !shell.isValid()) {
				logger.warn("Shell is invalid or not closed");
				return null;
			}

			LinearRing[] holes = null;
			if (linearRings.length > 1) {
				holes = Arrays.copyOfRange(linearRings, 1, linearRings.length);

				for (int j = 0; j < holes.length; j++) {
					if (!holes[j].isClosed() || !holes[j].isValid()) {
						logger.warn("Hole " + j + " is invalid or not closed");
						return null;
					}
				}
			}

			polygons[i] = geometryFactory.createPolygon(shell, holes);
		}

		return geometryFactory.createMultiPolygon(polygons);
	}

}
