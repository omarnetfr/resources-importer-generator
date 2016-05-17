<%--
/**
 * Copyright (c) 2016 Omar HADDOUCHI All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>
<%@include file="/html/sitemapgenerator/init.jsp" %>

<liferay-ui:error key="company-group-exception" message="company-group-exception"></liferay-ui:error>

<%
	String sitemap = (String)renderRequest.getAttribute("sitemap");
	String autoDeployDestDir = (String)renderRequest.getAttribute("autoDeployDestDir");
	
	String mimeTypesWebImages = DocumentUtil.MIME_TYPES_WEB_IMAGES;
	
	if (SessionErrors.isEmpty(renderRequest)) {
%>
	<aui:fieldset label="resources-importer-explorer">
		<div id="<portlet:namespace />resourcesImporterTree"></div>
	</aui:fieldset>

	<aui:fieldset id="sitemap-source" label="resources-importer-viewer">
		<div class="sitemap-source" id="<portlet:namespace />editor"></div>
		<div>
			<img id="<portlet:namespace />image" src="" />
		</div>
	</aui:fieldset>
	
	<portlet:actionURL name="saveResourceImporter" var="saveResourceImporterURL"></portlet:actionURL>
	
	<aui:form action="<%= saveResourceImporterURL %>" method="post">
		<aui:input name="sitemap" type="hidden"></aui:input>
		
		<aui:fieldset label="resources-importer-fieldset">
			<aui:field-wrapper cssClass="input-prepend input-append" helpMessage='<%= LanguageUtil.format(pageContext, "resources-importer-help", "") %>' label="resources-importer-name" name="resourceImporterURL">
				<span class="add-on"><liferay-ui:message key="<%= StringUtil.shorten(autoDeployDestDir.toString(), 60) %>" /></span>
			
				<aui:input cssClass="form-control" name="resourceImporterURL" label="" placeholder="theme-path-placeholder" value="<%= resourceImporterURL %>">
					<aui:validator name="required"></aui:validator>
				</aui:input>
				
				<span class="add-on"><liferay-ui:message key="resources-importer-path" /></span>
			</aui:field-wrapper>
			
			<aui:field-wrapper cssClass="input-prepend input-append" helpMessage='<%= LanguageUtil.format(pageContext, "plugin-source-help", "") %>' label="plugin-source-name" name="pluginSourceURL">
				<aui:input cssClass="form-control" name="pluginSourceURL" label="" placeholder="plugin-source-placeholder" value="<%= pluginSourceURL %>">
					<aui:validator name="required"></aui:validator>
				</aui:input>
				
				<span class="add-on"><liferay-ui:message key="plugin-source-path" /></span>
			</aui:field-wrapper>
			
			<aui:button-row>
				<aui:button type="submit" value="save" />
			</aui:button-row>
			
		</aui:fieldset>
	</aui:form>
		
	<aui:script use="aui-ace-editor,aui-tree-view,aui-io-request,aui-io-plugin-deprecated,datatype-xml,dataschema-xml,aui-loading-mask,aui-loading-mask-deprecated">
	
		var Util = Liferay.Util;
		
		var History = Liferay.HistoryManager;
		
		var EVENT_CLICK = 'click';
		
		var editorHeight = 580;
		
		var winHeight = A.one("body").get("winHeight");
		
		var sourceContainer = A.one('#<portlet:namespace />sitemap-source');
		
		var imageViewer = A.one('#<portlet:namespace />image');
		
		imageViewer.hide();
		
		var editorViewer = A.one('#<portlet:namespace />editor');
		
		if (winHeight < 710) {
			editorHeight = winHeight - 130;
		}
		
		var mimeTypesWebImages = '<%= mimeTypesWebImages %>';
		mimeTypesWebImages = mimeTypesWebImages.split(',');
		
		var RESOURCE_URL = decodeURIComponent('<portlet:resourceURL id="{resourceId}"></portlet:resourceURL>');
		var TREE_RESOURCE_URL = '<portlet:resourceURL />';
		
		var script = formatJSON('<%= HtmlUtil.escapeJS(sitemap) %>');
	
		var editor = new A.AceEditor(
			{
				boundingBox: '#<portlet:namespace />editor',
				height: editorHeight,
				width: '100%',
				mode: 'json',
				tabSize: 4,
				value: script,
				readOnly: true
			}
		).render();

		function formatJSON(jsonText) {
	
			if (jsonText.length > 0) {
				try {
					if (jsonlint.parse(jsonText)) {
						
						return vkbeautify.json(jsonText, 4);
					}
				} catch (e) {
					
				}
			}
		}

		var TreeUtil = {
			PAGINATION_LIMIT: 2,
			PREFIX_NAME: 'name_',
			createLink: function(data) {
				
				if (!data.href) {
					data.href = "";
				}
				
				if (!data.label) {
					data.label = "";
				}
				
				if (!data.resourceId) {
					data.resourceId = "";
				}
				
				if (!data.uuid) {
					data.uuid = "";
				}
				
				if (!data.document) {
					data.document = "";
				}
				
				return '<a href="' + data.href + '" data-resourceId="' + data.resourceId + '" class="tree-link" data-uuid="' + data.uuid + '" data-document="' + data.document + '">' + data.label + '</a>';
			},
			createId: function(name) {
				return TreeUtil.PREFIX_NAME + name.replace('/', '_');
			},
			extractName: function(node) {
				return node.get('id').match(/name_(.+)/)[1];
			},
			formatJSONResults: function(json) {
				var output = [];
				
				A.each(
					json.children,
					function(node) {
						
						var children = [];
						var total = 0;

						var nodeChildren = node.children;

						if (nodeChildren) {
							children = nodeChildren;
							total = children.length;
						}
						
						var leaf = node.leaf;
						
						if (!leaf) {
							leaf = false; 
						}
						
						var start = Math.max(children.length - TreeUtil.PAGINATION_LIMIT, 0);

						var newNode = {
							id: TreeUtil.createId(node.name),
							leaf: leaf,
							type: 'io',
							/*paginator: {
								limit: TreeUtil.PAGINATION_LIMIT,
								offsetParam: 'start',
								start: Math.max(children.length - TreeUtil.PAGINATION_LIMIT, 0),
								total: total
							},*/
							/*paginator: {
								limit: TreeUtil.PAGINATION_LIMIT,
								offsetParam: 'start',
								start: 0,
								total: 1
							},*/
							io: {
								cfg: {
									data: function(node) {
										return {
											cmd: 'get',
											p_p_resource_id: TreeUtil.extractName(node)
										};
									},
									method: A.config.io.method,
									on: {
										success: function(event, id, xhr) {
											var instance = this;

											var response;

											try {
												response = A.JSON.parse(xhr.responseText);
											}
											catch (e) {
											}
											
											if (response) {
												//instance.get('paginator').total = response.total;

												instance.syncUI();
											}
										}
									}
								},
								formatter: TreeUtil.formatJSONResults,
								url: TREE_RESOURCE_URL
							}
						}
						
						newNode.label = Util.escapeHTML(node.name);
						
						if (leaf) {
							newNode.label = TreeUtil.createLink(
								{
									label: newNode.label,
									resourceId: node.name,
									uuid: node.uuid,
									document: node.document
								}
							);
						}

						if (nodeChildren) {
							newNode.children = TreeUtil.formatJSONResults(node);
						}

						output.push(newNode);
					}
				);

				return output;
			}
		};
		
		var resourceImporterChildren = <%= TreeUtil.buildJSONTree(themeDisplay, false, StringPool.BLANK, StringPool.BLANK) %>;
		
		var rootNode = [
			{
				alwaysShowHitArea : true,
				children : TreeUtil.formatJSONResults(resourceImporterChildren),
				draggable : false,
				expanded : true,
				label : "resources-importer",
				leaf : false,
				type : 'file'
			}
		];

		var treeview = new A.TreeView({
			boundingBox : '#<portlet:namespace />resourcesImporterTree',
			children : rootNode
		}).render();
		
		var displayEditor = function(resourceId, response) {
			
			imageViewer.hide();
			editorViewer.show();
			
			var content = '', mode ='';
			
			if (resourceId.indexOf('.json') != -1 || resourceId.indexOf('.js') != -1) {
				
				content = formatJSON(response);
				mode = 'ace/mode/json';
				
			} else if (resourceId.indexOf('.html') != -1) {
				
				content = JSON.parse(response).content;
				mode = 'ace/mode/html';
				
			} else if (resourceId.indexOf('.xml') != -1) {
				
				content = JSON.parse(response).content;
				mode = 'ace/mode/xml';
			} else if (resourceId.indexOf('.vm') != -1) {
				
				content = JSON.parse(response).content;
				var mode = 'ace/mode/velocity';
				
			} else if (resourceId.indexOf('.ftl') != -1) {
				
				content = JSON.parse(response).content;
				mode = 'ace/mode/ftl';
				
			} else if (resourceId.indexOf('.xsl') != -1) {
				
				content = JSON.parse(response).content;
				mode = 'ace/mode/xsl';
				
			} else {
				
				content = JSON.parse(response).content;
				mode = 'ace/mode/text';
			}
			
			editor.getSession().setMode(mode);
			editor.getSession().setValue(content);

		};

		var displayImage = function(response) {
			
			imageViewer.show();
			editorViewer.hide();
			
			A.one('#<portlet:namespace />image').attr('src', 'data:image/png;base64,' + response);
		};

		A.one('#<portlet:namespace />resourcesImporterTree').delegate(
			'click',
			function(event) {
				event.preventDefault();

				var link = event.currentTarget.one('a');
				
				if (link && event.target.hasClass('tree-link')) {

					var resourceId = link.getData('resourceId');
					var uuid = link.getData('uuid');
					var document = link.getData('document');

					var requestURI = A.Lang.sub(
						RESOURCE_URL,
						{
							resourceId: resourceId
						}
					);
					
					console.log('resourceId : ' + resourceId);
					
					if ( !sourceContainer.loadingmask) {
						sourceContainer.plug(A.LoadingMask);
					}
					
					sourceContainer.loadingmask.show();
					
					A.io.request(
						requestURI,
						{
							data: {
								<portlet:namespace />uuid: uuid,
								<portlet:namespace />document: document
							},
							after: {
								failure: function(event, id, obj) {
									
								},
								success: function(event, id, obj) {
									var response = this.get('responseData');
																		
									var contentType = obj.getResponseHeader("content-type") || "";
									
									contentType = contentType.split(';');
									
									console.log(contentType);
									
									console.log(mimeTypesWebImages);
									
									if (mimeTypesWebImages.indexOf(contentType[0]) > -1) {
										displayImage(response);
									} else {
										//call function
										displayEditor(resourceId, response);
									}

									sourceContainer.loadingmask.hide();
								}
							}
						}
					);
				}
			},
			'.tree-node-content'
		);
	</aui:script>
<% } %>