var messages = angular.module('gazetteer.messages', []);

messages.factory('messages', function(){
	return {
		"ui.error": "Error",
		"ui.contactAdmin": "Please contact arachne@uni-koeln.de if the problem persists.",
		"ui.search.results": "Search result",
		"ui.search.hits": "Hits",
		"ui.search.limit.10.tooltip": "Show 10 hits per page",
		"ui.search.limit.50.tooltip": "Show 50 hits per page",
		"ui.search.limit.100.tooltip": "Show 100 hits per page",
		"ui.search.limit.1000.tooltip": "Show 1000 hits per page",
		"ui.search.sort.score.tooltip": "Sort by relevance",
		"ui.search.sort.id.tooltip": "Sort by ID",
		"ui.search.sort.name.tooltip": "Sort by name",
		"ui.search.sort.thesaurus.tooltip": "sort by thesaurus",
		"ui.place.children.search": "Show places in search",
		"ui.place.save.success": "Successfully created place",
		"ui.place.save.failure": "Could not create place",
		"ui.place.save.failure.parentError": "A place may not fall within itself or a place it contains",
		"ui.place.remove.success": "Successfully deleted place",
		"ui.place.remove.failure": "Could not delete place",
		"ui.place.protected-site-info": "Please log in to gain access to the exact coordinates.",
		"ui.thesaurus": "Thesaurus",
		"ui.link.tooltip": "Link to current place",
		"ui.place.deleted": "This place has been deleted",
		"ui.place.provenance-info": "This object contains data derived from the specified sources.",
		"ui.merge.tooltip": "Merge this and the current place",
		"ui.merge.success.head": "Successfully merged places",
		"ui.merge.success.body": "Please review the information of the newly created place",
		"ui.extendedSearch": "Extended search",
		"ui.change-history.change-type.create": "Created",
		"ui.change-history.change-type.edit": "Edited",
		"ui.change-history.change-type.delete": "Deleted",
		"ui.change-history.change-type.unknown": "Unknown",
		"place.name.ancient": "Ancient",
		"place.name.transliterated": "Transliterated",
		"place.types.archaeological-site": "Archaeological site",
		"place.types.archaeological-area": "Archaeological area",
		"place.types.continent": "Continent",
		"place.types.administrative-unit": "Administrative unit",
		"place.types.populated-place": "Populated place",
		"place.types.museum": "Building/Institution",
		"place.types.landform": "Landform",
		"place.types.island": "Island",
		"place.types.hydrography": "Hydrography",
		"place.types.landcover": "Landcover",
		"place.types.description.archaeological-site": "Site with archaeologically relevant structures",
		"place.types.description.archaeological-area": "Archaeologically defined cultural area and historical administrative units",
		"place.types.description.continent": "Landmass separated by natural or historical borders",
		"place.types.description.administrative-unit": "Politically defined administrative unit",
		"place.types.description.populated-place": "Place inhabited by humans",
		"place.types.description.museum": "Location of museums and other institutions",
		"place.types.description.landform": "Geomorphologic terrain feature",
		"place.types.description.island": "Landmass surrounded entirely by water (smaller than continents)",
		"place.types.description.hydrography": "All bigger masses of water (standing and running)",
		"place.types.description.landcover": "Physical and biological landcover",
		"place.types.groups.physical-geographic": "Physical geographic units",
		"place.types.groups.human-geographic": "Human geographic units",
		"place.types.groups.archaeological": "Archaeological/cultural-historical units",
		"place.types.groups.building": "Building",
		"location.confidence.0": "Not specified",
		"location.confidence.1": "Uncertain",
		"location.confidence.2": "Certain",
		"location.confidence.3": "Exact",
		"location.public": "Public coordinates",
		"languages.deu": "German",
		"languages.eng": "English",
		"languages.ita": "Italian",
		"languages.fra": "French",
		"languages.ell": "Greek (Modern)",
		"languages.lat": "Latin",
		"languages.grc": "Ancient Greek",
		"languages.sqi": "Albanian",
		"languages.pol": "Polish",
		"languages.tur": "Turkish",
		"languages.ara": "Arabian",
		"languages.spa": "Spanish",
		"languages.por": "Portuguese",
		"languages.zho": "Chinese",
		"languages.rus": "Russian",
		"languages.vie": "Vietnamese",
		"domain.place.parent": "Falls within",
		"domain.place.type": "Type",
		"domain.place.tags": "Tags"
	};
});