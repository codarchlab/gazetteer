<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>
	<div style="margin-top: 3px;">
		<div ng-show="shape && !deactivated" class="label" style="margin-right: 5px; background-color: #6786ad;">
			<s:message code="ui.shapeEditor.polygonPresent" text="ui.shapeEditor.polygonPresent"/>
		</div>
		<div ng-show="shape && deactivated" class="label" style="margin-right: 5px;">
			<s:message code="ui.shapeEditor.polygonPresent" text="ui.shapeEditor.polygonPresent"/>
		</div>
		<div ng-hide="shape" class="label" style="margin-right: 5px;">
			<s:message code="ui.shapeEditor.polygonNotPresent" text="ui.shapeEditor.polygonNotPresent"/>
		</div>
		
		<div ng-if="!deactivated && !shape" class="btn-group" role="group">
			<button class="btn btn-default shape-edit-btn" type="button" ng-click="openMapOverlay()">
				<span class="fa fa-pencil"></span>
			</button>
			<button class="btn btn-default shape-edit-btn" type="button" ng-click="openTextInputOverlay()">
				<span class="fa fa-i-cursor" style="padding-left: 1px;"></span>
			</button>
			<button class="btn btn-default shape-edit-btn disabled" type="button">
				<span class="fa fa-remove"></span>
			</button>
		</div>
		
		<div ng-if="!deactivated && shape" class="btn-group" role="group">
			<button class="btn btn-default shape-edit-btn" type="button" ng-click="openMapOverlay()">
				<span class="fa fa-pencil"></span>
			</button>
			<button class="btn btn-default shape-edit-btn" type="button" ng-click="openTextInputOverlay()">
				<span class="fa fa-i-cursor" style="padding-left: 1px;"></span>
			</button>
			<button class="btn btn-default shape-edit-btn" type="button" href="#deleteShapeModal_{{editorName}}" data-toggle="modal">
				<span class="fa fa-remove"></span>
			</button>
		</div>
		
		<div ng-if="deactivated" class="btn-group" role="group">
			<button class="btn btn-default shape-edit-btn disabled" type="button">
				<span class="fa fa-pencil"></span>
			</button>
			<button class="btn btn-default shape-edit-btn disabled" type="button">
				<span class="fa fa-i-cursor" style="padding-left: 1px;"></span>
			</button>
			<button class="btn btn-default shape-edit-btn disabled" type="button">
				<span class="fa fa-remove"></span>
			</button>
		</div>
	</div>
	
	<div modal="showMapOverlay" close="closeMapOverlay()">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal" ng-click="closeMapOverlay()">×</button>
			<h3 ng-show="shape"><s:message code="ui.shapeEditor.editPolygon" text="ui.shapeEditor.editPolygon" /></h3>
			<h3 ng-hide="shape"><s:message code="ui.shapeEditor.createPolygon" text="ui.shapeEditor.createPolygon" /></h3>
		</div>
 		<div class="modal-body gmap">
 			<div id="shape_editor_map_canvas" style="height: 400px" ui-map="map" ui-options="mapOptions" ng-mousemove="resize()" ng-mouseup="setUpdateMapPropertiesTimer()"></div>
 		</div>
 		<div class="modal-footer">
 			<button type="button" class="btn btn-primary" ng-click="saveShape()"><s:message code="ui.ok" text="ui.ok" /></button>
 			<button type="button" class="btn" ng-click="closeMapOverlay()"><s:message code="ui.cancel" text="ui.cancel" /></button>
 		</div>
	</div>
	
	<div modal="showTextInputOverlay" close="closeTextInputOverlay()">
		<span>
			<div class="modal-header">
				<button ng-if="loading == 0" type="button" class="close" ng-click="closeTextInputOverlay()">&times;</button>
				<button ng-if="loading > 0" type="button" class="close disabled">&times;</button>
				<h3><s:message code="ui.shapeEditor.insertShapeCoordinates" text="ui.shapeEditor.insertShapeCoordinates"/></h3>
			</div>
			<div class="modal-body">
				<form class="form-horizontal">
					<div class="control-group" style="margin-top: 10px;">
						<label class="control-label"><s:message code="ui.shapeEditor.coordinatesFormat" text="ui.shapeEditor.coordinatesFormat" /></label>
						<div class="controls">
							<select ng-show="loading == 0" ng-model="coordinatesStringFormat" ng-change="reloadCoordinatesString()">
								<option value="geojson">GeoJSON</option>
								<option value="wkt">WKT</option>
							</select>
							
							<select ng-show="loading > 0" ng-model="coordinatesStringFormat" disabled>
								<option value="geojson">GeoJSON</option>
								<option value="wkt">WKT</option>
							</select>
						</div>
					</div>
				</form>
				<textarea type="text" rows="7" style="width: 100%; box-sizing: border-box" ng-model="coordinatesString" />
				<div ng-if="loading > 0">
					<i class="icon-spinner icon-spin icon-large" style="color: #6786ad; cursor: default;"></i>
					<span style="font-style: italic; margin-left: 3px;"><s:message code="ui.shapeEditor.creatingPolygon" text="ui.shapeEditor.creatingPolygon" /></span>
				</div>
				<div ng-if="parsingError">
					<div class="well" style="margin-top: 12px; margin-bottom: 10px;">
						<div style="font-weight: bold; margin-bottom: 5px;"><s:message code="ui.shapeEditor.error" text="ui.shapeEditor.error"/>:</div>
						<div ng-if="!parsingError.data">
							<span gaz-translate="'ui.shape-editor.error.' + parsingError.msgKey"></span>
						</div>
						<div ng-if="parsingError.data">
							<span gaz-translate="'ui.shape-editor.error.' + parsingError.msgKey + '.1'"></span>{{parsingError.data}}<span gaz-translate="'ui.shape-editor.error.' + parsingError.msgKey + '.2'"></span>
						</div>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<a ng-if="loading == 0" ng-click="closeTextInputOverlay()" class="btn btn-default"><s:message code="ui.cancel" text="ui.cancel"/></a>
				<a ng-if="loading == 0" ng-click="parseCoordinatesString()" class="btn btn-primary"><s:message code="ui.save" text="ui.save"/></a>
				<a ng-if="loading > 0" class="btn btn-default disabled"><s:message code="ui.cancel" text="ui.cancel"/></a>
				<a ng-if="loading > 0" class="btn btn-primary disabled"><s:message code="ui.save" text="ui.save"/></a>
			</div>
		</span>
	</div>
	
	<div class="modal hide fade" id="deleteShapeModal_{{editorName}}">
		<span>
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h3><s:message code="ui.deleteShape" text="ui.deleteShape"/>?</h3>
			</div>
			<div class="modal-body">
				<p><s:message code="ui.deleteShape.really" text="ui.deleteShape.really"/></p>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><s:message code="ui.cancel" text="ui.cancel"/></a>
				<a ng-click="deleteShape(shape)" data-dismiss="modal" class="btn btn-danger"><s:message code="ui.delete" text="ui.delete"/></a>
			</div>
		</span>
	</div>
</div>