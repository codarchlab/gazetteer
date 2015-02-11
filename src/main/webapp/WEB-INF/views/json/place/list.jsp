<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*,
	java.util.List, java.util.ArrayList, java.util.Map,
	org.dainst.gazetteer.converter.JsonPlaceSerializer,
	org.dainst.gazetteer.dao.*" %>

<% 

response.setHeader("Content-Type", "application/json; charset=utf-8"); 

List<Place> places = (List<Place>) request.getAttribute("places");
Map<String, List<Place>> parents = (Map<String, List<Place>>) request.getAttribute("parents");
String baseUri = (String) request.getAttribute("baseUri");
Long hits = (Long) request.getAttribute("hits");
String queryId = (String) request.getAttribute("queryId");
Boolean includePolygons = (Boolean) request.getAttribute("includePolygons");
if (includePolygons == null) includePolygons = true;

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri);

StringBuilder sb = new StringBuilder("{");
sb.append("\"total\": ").append(hits);
sb.append(", \"result\": [");
int numberOfPlaces = 0;
List<String> accessGrantedPlaces = new ArrayList<String>();
List<String> accessDeniedPlaces = new ArrayList<String>();
for (Place place : places) {
	List<Place> placeParents = null;
	if (parents != null)
		placeParents = parents.get(place.getId());
	String serializedPlace = serializer.serialize(place, placeParents, includePolygons);
	if (serializedPlace != null) {
		if (serializedPlace.indexOf("\"accessDenied\":true") > 0)		
			accessDeniedPlaces.add(serializedPlace);
		else
			accessGrantedPlaces.add(serializedPlace);
		numberOfPlaces++;
	}	
}
int appendedPlaces = 0;
for (String serializedPlace : accessGrantedPlaces) {
	sb.append(serializedPlace);
	appendedPlaces++;
	if (numberOfPlaces > appendedPlaces)
		sb.append(",");
}
for (String serializedPlace : accessDeniedPlaces) {
	sb.append(serializedPlace);
	appendedPlaces++;
	if (numberOfPlaces > appendedPlaces)
		sb.append(",");
}
sb.append("]");

Map<String, List<String[]>> facets = (Map<String, List<String[]>>) request.getAttribute("facets");
int i = 0;
if (facets != null) {
	sb.append(", \"facets\": {");
	for (Map.Entry<String,List<String[]>> facet : facets.entrySet()) {
		if (facet.getValue().size() == 0) {
			continue;
		}
		sb.append("\"").append(facet.getKey()).append("\": [");
		int j = 0;
		for (String[] entry : facet.getValue()) {
		    sb.append(String.format("{ \"label\": \"%s\", \"term\": \"%s\", \"count\": %s }", entry[0], entry[1], entry[2]));
		    if(++j < facet.getValue().size()) sb.append(",");
		}
		sb.append("]");
		if(++i < facets.size()) sb.append(",");
	}
	if (sb.charAt(sb.length()-1) == ',')
		sb.deleteCharAt(sb.length()-1);
	sb.append("}");
}

if (queryId != null)
	sb.append(", \"queryId\": \"" + queryId + "\"");

sb.append("}");

%>

<%= sb.toString() %>