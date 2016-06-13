<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<div class="form-group">
	<div class="btn-group btn-group-xs markdown-editor-button-group">
		<button class="btn btn-default" ng-click="formatText('bold')">
			<span class="fa fa-bold"></span>
		</button>
		<button class="btn btn-default" ng-click="formatText('italic')">
			<span class="fa fa-italic"></span>
		</button>
		<button class="btn btn-default heading-button" ng-click="formatText('heading1')">
			<span class="fa fa-header"><span class="heading-button-number">1</span></span>
		</button>
		<button class="btn btn-default heading-button" ng-click="formatText('heading2')">
			<span class="fa fa-header"><span class="heading-button-number">2</span></span>
		</button>
		<button class="btn btn-default heading-button" ng-click="formatText('heading3')">
			<span class="fa fa-header"><span class="heading-button-number">3</span></span>
		</button>
		<button class="btn btn-default" ng-click="formatText('listBullets')">
			<span class="fa fa-list-ul"></span>
		</button>
		<button class="btn btn-default" ng-click="formatText('listNumbers')">
			<span class="fa fa-list-ol"></span>
		</button>
		<button class="btn btn-default" data-toggle="modal" href="#markdownLinkModal" ng-click="formatText('link')">
			<span class="fa fa-link"></span>
		</button>
	</div>
	<div>
		<textarea type="text" class="form-control" ng-model="markdownText" ng-trim="false" placeholder="{{placeholder}}" rows="10" style="width: 500px;"></textarea>
	</div>

	<div class="modal fade hide" id="markdownLinkModal">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal">�</button>
			<h3><s:message code="ui.markdownLinkModal.header" text="ui.markdownLinkModal.header" /></h3>
		</div>
		<div class="modal-body">
			<s:message code="ui.markdownLinkModal.targetUrl" text="ui.markdownLinkModal.targetUrl" />
			<div class="form-group">
				<input type="label" class="form-control" ng-model="link.url" placeholder="http://www.example.com">
			</div>
			<s:message code="ui.markdownLinkModal.linkDescription" text="ui.markdownLinkModal.linkDescription" />
			<div class="form-group">
				<input type="label" class="form-control" ng-model="link.description" placeholder="">
			</div>
		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><s:message code="ui.cancel" text="ui.cancel"/></a>
			<a ng-click="createLink()" data-dismiss="modal" class="btn btn-primary"><s:message code="ui.ok" text="ui.ok"/></a>
		</div>
	</div>

</div>