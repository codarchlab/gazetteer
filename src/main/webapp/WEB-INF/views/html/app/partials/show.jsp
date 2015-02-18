<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div gaz-place-nav active-tab="show" place="place"></div>

<s:message code="ui.copyToClipboard" var="copyMsg"/>
			
<div class="modal hide" id="copyUriModal">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">×</button>
		<h3><s:message code="ui.copyToClipboardHeading"/></h3>
	</div>
	<div class="modal-body">
		<label>${copyMsg}</label>
		<input class="input-xxlarge" style="width:97%" type="text" value="${baseUri}place/{{place.gazId}}" id="copyUriInput"></input>
	</div>
</div>
<script type="text/javascript">
	$("#copyUriModal").on("shown",function() {
		$("#copyUriInput").focus().select();
	});
</script>

<h3><s:message code="ui.information" text="ui.information"/></h3>

<dl class="dl-horizontal">

	<span ng-show="place.prefName || (place.names && place.names.length > 0)">
		<dt><s:message code="domain.place.names" /></dt>
		<span ng-show="place.prefName">
			<dd>
				<em><s:message code="domain.place.prefName" text="domain.place.prefName"/>: </em>
				{{place.prefName.title}}
				<span ng-show="place.prefName.ancient">
					(<small gaz-translate="'place.name.ancient'"></small>)
				</span>
				<small ng-show="place.prefName.language">
					<c:forEach var="language" items="${languages}">
						<em ng-show="'${language.key}' == place.prefName.language">${language.value}</em>
					</c:forEach>
				</small>
				<em ng-show="place.prefName.transliterated">
					(<small gaz-translate="'place.name.transliterated'"></small>)
				</em>
			</dd>
		</span>
		<dd ng-repeat="placename in place.names | orderBy:['language','title'] | limitTo: namesDisplayed">
			{{placename.title}}
			<span ng-show="placename.ancient">
				(<small gaz-translate="'place.name.ancient'"></small>)
			</span>
			<small ng-hide="!placename.language">
				<c:forEach var="language" items="${languages}">
					<em ng-show="'${language.key}' == placename.language">${language.value}</em>
				</c:forEach>
			</small>
			<em ng-show="placename.transliterated">
				(<small gaz-translate="'place.name.transliterated'"></small>)
			</em>
		</dd>
		<dd ng-show="place.names.length > 4">
			<a href="" ng-click="changeNumberOfDisplayedNames()">
				<small ng-show="namesDisplayed == 4">(<span gaz-translate="'ui.place.names.more'"></span>)</small>
				<small ng-hide="namesDisplayed == 4">(<span gaz-translate="'ui.place.names.less'"></span>)</small>
			</a>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.tags">
		<dt><s:message code="domain.place.tags" text="domain.place.tags" /></dt>
		<dd>
			<span ng-repeat="tag in place.tags">
				<a class="label label-info" href="#!/search?q=%7B%22bool%22:%7B%22must%22:%5B%7B%22query_string%22:%7B%22query%22:%22tags:{{tag}}%22%7D%7D%5D%7D%7D&type=extended">{{tag}}</a>&nbsp; 
			</span>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.provenance">
		<dt><s:message code="domain.place.provenance" text="domain.place.provenance" /></dt>
		<dd>
			<span ng-repeat="provenanceEntry in place.provenance">
				<a class="label label-info"  href="#!/search?q=%7B%22bool%22:%7B%22must%22:%5B%7B%22query_string%22:%7B%22query%22:%22provenance:{{provenanceEntry}}%22%7D%7D%5D%7D%7D&type=extended">{{provenanceEntry}}</a>&nbsp; 
			</span>
			<i class="icon-info-sign" style="color: #5572a1; cursor: pointer;" gaz-tooltip="'ui.place.provenance-info'"></i>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.parents || place.parents.length == 0" ng-cloak>
		<dt><s:message code="domain.place.parent" text="domain.place.parent" /></dt>
		<dd ng-repeat="parent in place.parents | reverse">
			<div style="margin-left: {{$index * 16}}px;"><i ng-show="$index != 0" class="icon-circle-arrow-right" style="cursor: default;"></i><div gaz-place-title place="parent"></div></div>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!children || children.length < 1">
		<dt><s:message code="domain.place.children" text="domain.place.children" /></dt>
		<dd style="cursor: default;">
			<em><s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="{{totalChildren}}" />:</em>
			<a gaz-tooltip="'ui.place.children.search'" ng-href="#!/search?q=parent:{{place.gazId}}"><i class="icon-search"></i></a>
			<i class="icon-circle-arrow-left muted" ng-show="offsetChildren == 0"></i>
			<i ng-click="prevChildren()" class="icon-circle-arrow-left" style="color: #5572a1; cursor: pointer;" ng-hide="offsetChildren == 0"></i>
			<i class="icon-circle-arrow-right muted" ng-show="offsetChildren+10 >= totalChildren"></i>
			<i ng-click="nextChildren()" class="icon-circle-arrow-right" style="color: #5572a1; cursor: pointer;" ng-hide="offsetChildren+10 >= totalChildren"/></i>
		</dd>
		<dd>
			<ul>
				<li ng-repeat="child in children">
					<div gaz-place-title place="child"></div>
				</li>
			</ul>
		</dd>
		<br/>
	</span>					
	
	<span ng-hide="!relatedPlaces || relatedPlaces.length < 1">
		<dt><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" /></dt>
		<dd>
			<em><s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="{{totalRelatedPlaces}}" />:</em>
			<a gaz-tooltip="'ui.place.children.search'" ng-href="#!/search?q=relatedPlaces:{{place.gazId}}"><i class="icon-search"></i></a>
			<i class="icon-circle-arrow-left" ng-show="offsetRelatedPlaces == 0"></i>
			<a ng-click="prevRelatedPlaces()" ng-hide="offsetRelatedPlaces == 0"><i class="icon-circle-arrow-left"/></i></a>
			<i class="icon-circle-arrow-right" ng-show="offsetRelatedPlaces+10 >= totalRelatedPlaces"></i>
			<a ng-click="nextRelatedPlaces()" ng-hide="offsetRelatedPlaces+10 >= totalRelatedPlaces"><i class="icon-circle-arrow-right"/></i></a>
		</dd>
		<dd>
			<ul>
				<li ng-repeat="relatedPlace in relatedPlaces">
					<div gaz-place-title place="relatedPlace"></div>
				</li>
			</ul>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.prefLocation || !((place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.shape)">
		<dt><s:message code="domain.place.locations" text="domain.place.locations" /></dt>
		<dd>
			<span ng-show="place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0"><em><s:message code="domain.location.latitude" text="domain.location.latitude" />: </em>{{place.prefLocation.coordinates[1]}},
				<em><s:message code="domain.location.longitude" text="domain.location.longitude" />: </em>{{place.prefLocation.coordinates[0]}}</span><span ng-show="place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0 && place.prefLocation.altitude">,</span>
				<span ng-show="place.prefLocation.altitude"><em><s:message code="domain.location.altitude" text="domain.location.altitude" />: </em>{{place.prefLocation.altitude}} m</span>
				<span ng-show="place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0 && !place.prefLocation.publicSite">
					<sec:authorize access="hasRole('ROLE_USER')">
						(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
						<span gaz-translate="'location.confidence.'+place.prefLocation.confidence"></span>)
					</sec:authorize>
					<sec:authorize access="!hasRole('ROLE_USER')">
						(<span><s:message code="domain.location.rounded" text="domain.location.rounded" /> <i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.protected-site-info'"></i></span>)
					</sec:authorize>
				</span>
				<span ng-hide="!place.prefLocation.publicSite">
					(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />: </em>
					<span gaz-translate="'location.confidence.'+place.prefLocation.confidence"></span>)
				</span>
				<br ng-show="(place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.altitude"/>
			</span>
			<em ng-show="place.prefLocation.shape" ng-mouseover="setHighlight(place.gazId + '#p')" ng-mouseout="setHighlight(null)" style="text-decoration:none; border-bottom: 1px dotted black; cursor: pointer;"><s:message code="domain.location.polygonSpecified" text="domain.location.polygonSpecified" /></em>
		</dd>
		<span ng-hide="!place.locations">
			<br />
			<dt><s:message code="domain.place.otherLocations" text="domain.place.otherLocations" /></dt>
			<dd>
				<ul>
					<li ng-repeat="location in place.locations">
						<span ng-show="location.coordinates">
							<em><s:message code="domain.location.latitude" text="domain.location.latitude" />: </em>{{location.coordinates[1]}},&nbsp;
							<em><s:message code="domain.location.longitude" text="domain.location.longitude" />: </em>{{location.coordinates[0]}}</span><span ng-show="location.coordinates && location.altitude">,&nbsp;</span>
						<span ng-show="location.altitude"><em><s:message code="domain.location.altitude" text="domain.location.altitude" />: </em>{{location.altitude}} m</span><span ng-show="(location.coordinates || location.altitude) && location.shape">,</span>
						<em ng-show="location.shape" ng-mouseover="setHighlight(place.gazId + '#' + $index)" ng-mouseout="setHighlight(null)" style="text-decoration:none; border-bottom: 1px dotted black; cursor: pointer;"><s:message code="domain.location.polygonSpecified" text="domain.location.polygonSpecified" /></em>
						<span ng-show="!location.publicSite">
							<sec:authorize access="hasRole('ROLE_USER')">
								(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
								<span gaz-translate="'location.confidence.'+location.confidence"></span>)
							</sec:authorize>
							<sec:authorize access="!hasRole('ROLE_USER')">
								(<span><s:message code="domain.location.rounded" text="domain.location.rounded" /> <i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.protected-site-info'"></i></span>)
							</sec:authorize>
						</span>
						<span ng-hide="!location.publicSite">
							(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
							<span gaz-translate="'location.confidence.'+location.confidence"></span>)
						</span>
					</li>
				</ul>
			</dd>
		</span>
		<br />
	</span>
	
	<span ng-hide="!place.types || place.types.length == 0">
		<dt><s:message code="domain.place.type" text="domain.place.type" /></dt>
		<c:forEach var="placeType" items="${placeTypes}">
			<dd ng-show="hasType('${placeType}')">
				<span gaz-translate="'place.types.' + '${placeType}'"></span>
				<br/>
			</dd>
		</c:forEach>
		<br/>
	</span>
	
	<span ng-hide="!place.recordGroupId || place.recordGroupId.length == 0">
		<dt><s:message code="domain.place.recordGroup" text="domain.place.recordGroup" /></dt>
		<c:forEach var="recordGroup" items="${recordGroups}">
			<dd ng-show="'${recordGroup.id}' == place.recordGroupId">${recordGroup.name}</dd>
		</c:forEach>
		<br/>
	</span>
	
	<span>
		<dt><s:message code="ui.contexts" text="ui.contexts"/></dt>
		<dd>
			<a ng-href="http://arachne.uni-koeln.de/arachne/index.php?view[layout]=search_result_overview&view[category]=overview&search[constraints]=Gazetteerid:%22{{place.gazId}}%22" target="_blank">
				<s:message code="ui.link.arachne" text="ui.link.arachne"/>
				<i class="icon-external-link"></i>
			</a>
		</dd>
		<dd ng-show="getIdsByContext('zenon-thesaurus') != false">
			<a ng-href="http://zenon.dainst.org/#search?q=f999_1:({{getIdsByContext('zenon-thesaurus').join(' OR ')}})" target="_blank">
				<s:message code="ui.link.zenon" text="ui.link.zenon"/>
				<i class="icon-external-link"></i>
			</a>
		</dd>
		<dd ng-show="getIdsByContext('pleiades') != false">
			<a ng-href="http://pelagios.org/api/places/http%3A%2F%2Fpleiades.stoa.org%2Fplaces%2F{{getIdsByContext('pleiades')[0]}}" target="_blank">
				<s:message code="ui.link.pelagios" text="ui.link.pelagios"/>
				<i class="icon-external-link"></i>
			</a>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.identifiers">
		<dt><s:message code="domain.place.identifiers" text="domain.place.identifiers" /></dt>
		<dd ng-repeat="identifier in place.identifiers | orderBy:['context','value']">
			<em>{{identifier.context}}:</em> {{identifier.value}}
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.links">
		<dt><s:message code="domain.place.links" text="domain.place.links" /></dt>
		<dd ng-repeat="link in place.links | orderBy:['predicate','object']">
			<em>{{link.predicate}}:</em> <a ng-href="{{link.object}}" target="_blank">{{decodeUri(link.object)}}</a>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.comments">
		<dt><s:message code="domain.place.comments" text="domain.place.comments" /></dt>
		<dd ng-repeat="comment in place.comments">
			<blockquote>{{comment.text}}</blockquote>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.noteReisestipendium">
		<dt><s:message code="domain.place.noteReisestipendium" text="domain.place.noteReisestipendium" /></dt>
		<dd>
			<blockquote ng-bind-html-unsafe="place.noteReisestipendium | parseUrlFilter | parseLineBreakFilter"></blockquote>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.commentsReisestipendium">
		<dt><s:message code="domain.place.commentsReisestipendium" text="domain.place.commentsReisestipendium" /></dt>
		<dd ng-repeat="comment in place.commentsReisestipendium">
			<blockquote>
				{{comment.text}}
				<small ng-show="comment.user && comment.user != null">{{comment.user}}</small>
			</blockquote>
		</dd>
		<br/>
	</span>
	
</dl>
