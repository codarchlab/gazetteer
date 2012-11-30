<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>

	<style type="text/css">
		.place-picker-field {
			background-colour: #fff;
			-webkit-border-radius: 3px 0 0 3px;
			-moz-border-radius: 3px 0 0 3px;
			border-radius: 3px 0 0 3px;
			border: 1px solid #CCC;
			height: 20px;
			padding: 4px 6px;
			font-size: 14px;
			line-height: 20px;
			width: 210px;
			display: inline-block;
		}
		.place-picker-btn {
			webkit-border-radius: 0 3px 3px 0;
			-moz-border-radius: 0 3px 3px 0;
			border-radius: 0 3px 3px 0;
			margin-left: -1px;
			vertical-align: top;
		}
	</style>
	
	<span>
	
		<div class="place-picker-field">
			<span ng-hide="place.gazId">
				<em><s:message code="ui.picker.pickAPlace" text="ui.picker.pickAPlace"/></em>
			</span>
			<a ng-show="place.gazId" href="#/show/{{parent.gazId}}">
				{{place.prefName.title}}
				<em ng-hide="!place.type">(<span gaz-translate="'place.types.' + place.type"></span>)</em>
			</a>
		</div><button class="btn gaz-pick-button place-picker-btn" type="button" ng-click="openOverlay()">
			<i class="icon-search"></i><i class="icon-globe"></i>
		</button>
	
	</span>
	
	<div class="gaz-pick-overlay-inner" ng-show="showOverlay">
		<div class="navbar navbar-inverse">
			<div class="navbar-inner">
				<form class="navbar-search pull-left" action="/gazetteer/place" autocomplete="off">
	 				<input type="text" class="search-query" placeholder="Suche" ng-model="search.q" autocomplete="off">
	 				<i class="icon-search icon-white"></i>
				</form>
			</div>
		</div>
		<div class="gaz-pick-results">
			<div class="gaz-pick-result-row" ng-repeat="place in places">
				<a ng-click="selectPlace(place)">{{place.prefName.title}} <em>(&#35;{{place.gazId}})</em></a>
			</div>
		</div>
	</div>

</span>