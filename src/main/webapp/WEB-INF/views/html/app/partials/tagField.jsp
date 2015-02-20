<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>

	<style type="text/css">
		.tag-field {
			background-color: #fcfcfc;
			border-radius: 0 0 0 0;
			border: 1px solid #CCC;
			height: 23px;
			padding	: 0px 4px 6px 4px;
			font-size: 14px;
			line-height: 20px;
			display: inline-block;
			overflow: hidden;
			white-space: nowrap;
		}
		.input {
    		border-width: 0px !important;
    		border: none !important;
    		white-space: nowrap;
    		width: 100%;
		}
		.remove-icon-mouseover {
			color: #d9e8ef;
		}
	</style>
	
	<span>
	
		<div class="tag-field" ng-style="{ width: fieldwidth }">
			<span ng-repeat="tag in tags"><span class="label label-info">{{tag}} <i class="icon-remove-sign" style="cursor: pointer;" ng-click="removeTag(tag)" ng-hide="mouseOver" ng-mouseenter="mouseOver = true" ng-mouseleave="mouseOver = false"></i><i class="icon-remove-sign remove-icon-mouseover" style="cursor: pointer;" ng-click="removeTag(tag)" ng-show="mouseOver" ng-mouseenter="mouseOver = true" ng-mouseleave="mouseOver = false"></i></span>&nbsp;</span><input type="text" name="tagTextField" class="input" ng-model="inputText" on-enter="chooseSuggestion()" on-backspace="backspace()" on-arrow-up="selectPreviousSuggestion()" on-arrow-down="selectNextSuggestion()" on-blur="lostFocus()"/>
		</div>
		<div style="position: absolute; left: {{textFieldPos + 5}}px; z-index: 2000" class="suggestion-menu" ng-show="suggestions">
			<div ng-repeat="suggestion in suggestions">
				<div class="suggestion" ng-mousedown="chooseSuggestion()" ng-hide="selectedSuggestionIndex == $index" ng-mouseover="setSelectedSuggestionIndex($index)">{{suggestion}}</div>
				<div class="suggestion selected" ng-mousedown="chooseSuggestion()" ng-show="selectedSuggestionIndex == $index">{{suggestion}}</div>
			</div>
		</div>
			
	</span>

</span>