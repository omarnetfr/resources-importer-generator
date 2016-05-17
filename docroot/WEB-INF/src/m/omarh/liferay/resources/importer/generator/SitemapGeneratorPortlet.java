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
package m.omarh.liferay.resources.importer.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ValidatorException;

import m.omarh.liferay.resources.importer.generator.util.AssetsUtil;
import m.omarh.liferay.resources.importer.generator.util.AutoDeployUtil;
import m.omarh.liferay.resources.importer.generator.util.JSONUtil;
import m.omarh.liferay.resources.importer.generator.util.SiteMapUtil;
import m.omarh.liferay.resources.importer.generator.util.tree.ApplicationDisplayUtil;
import m.omarh.liferay.resources.importer.generator.util.tree.DocumentUtil;
import m.omarh.liferay.resources.importer.generator.util.tree.JournalArticleUtil;
import m.omarh.liferay.resources.importer.generator.util.tree.JournalStructureUtil;
import m.omarh.liferay.resources.importer.generator.util.tree.JournalTemplateUtil;
import m.omarh.liferay.resources.importer.generator.util.tree.TreeUtil;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PrefsParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class SitemapGeneratorPortlet
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class SitemapGeneratorPortlet extends MVCPortlet {

	@Override
	public void doView(RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {
		
		ThemeDisplay themeDisplay = (ThemeDisplay)renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		
		Group scopeGroup = themeDisplay.getScopeGroup();
		
		if (scopeGroup.getGroupId() == themeDisplay.getCompanyGroupId()) {
			
			SessionMessages.add(renderRequest, PortalUtil.getPortletId(renderRequest) + SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
			SessionErrors.add(renderRequest, "company-group-exception");
			
			super.doView(renderRequest, renderResponse);
			
		} else {
			
			JSONObject sitemapJsonObject = SiteMapUtil.generateJSONSitemap(scopeGroup);
			
			renderRequest.setAttribute("autoDeployDestDir", AutoDeployUtil.getAutoDeployDestDir() + StringPool.SLASH);
			renderRequest.setAttribute("sitemap", JSONUtil.beautify(sitemapJsonObject.toString()));
			
			super.doView(renderRequest, renderResponse);
		}
	}
	
	@Override
	public void serveResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws IOException,
			PortletException {
		
		String resourceId = resourceRequest.getResourceID();
		
		boolean isDocument = ParamUtil.getBoolean(resourceRequest, "document");
		
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		
		if (resourceId.equals("sitemap.json")) {
			
			String sitemap = JSONUtil.valorizeVariables(themeDisplay, SiteMapUtil.generateJSONSitemap(themeDisplay.getScopeGroup()).toString());
			
			writeJSON(resourceRequest, resourceResponse, JSONUtil.beautify(sitemap));
			
		} else if (resourceId.equals("assets.json")) {
			
			String assets = JSONUtil.valorizeVariables(themeDisplay, AssetsUtil.generateJSONAsset(themeDisplay.getScopeGroup()).toString());
			
			writeJSON(resourceRequest, resourceResponse, JSONUtil.beautify(assets));
		
		} else if (resourceId.endsWith(".html") && !isDocument) {
			
			writeJSON(resourceRequest, resourceResponse, JournalArticleUtil.getJournalArticleContentJSONObject(themeDisplay, resourceId));
		
		} else if (resourceId.endsWith(".xml") && !isDocument) {
			
			String uuid = ParamUtil.getString(resourceRequest, "uuid");
			
			writeJSON(resourceRequest, resourceResponse, JournalStructureUtil.getJournalStructureContent(themeDisplay, uuid));
		
		} else if (resourceId.endsWith(".vm") || resourceId.endsWith(".ftl")) {
			
			String uuid = ParamUtil.getString(resourceRequest, "uuid");
			
			writeJSON(resourceRequest, resourceResponse, JournalTemplateUtil.getJournalTemplateContent(themeDisplay, uuid));
		
		} else if (resourceId.endsWith("articles_")) {

			writeJSON(resourceRequest, resourceResponse, JournalArticleUtil.buildJSONJournalArticles(themeDisplay, false, StringPool.BLANK, StringPool.BLANK));
		
		} else if (resourceId.endsWith("structures_")) {

			writeJSON(resourceRequest, resourceResponse, JournalStructureUtil.buildJSONDDMStrcutures(themeDisplay.getScopeGroup(), false, StringPool.BLANK, StringPool.BLANK));
			
		} else if (resourceId.endsWith("templates_")) {

			writeJSON(resourceRequest, resourceResponse, JournalTemplateUtil.buildJSONDDMTemplates(themeDisplay.getScopeGroup(), false, StringPool.BLANK, StringPool.BLANK));
			
		} else if (resourceId.endsWith("application_display_")) {

			writeJSON(resourceRequest, resourceResponse, ApplicationDisplayUtil.buildJSONApplicationDisplay(themeDisplay.getScopeGroup(), false, StringPool.BLANK, StringPool.BLANK));
			
		} else if (resourceId.endsWith("documents_")) {

			writeJSON(resourceRequest, resourceResponse, DocumentUtil.buildJSONDocuments(themeDisplay.getScopeGroup(), false, StringPool.BLANK, StringPool.BLANK));
		
		} else if (ArrayUtil.contains(DocumentUtil.TEXT_EXTENSIONS, FileUtil.getExtension(resourceId))) {
			
			String uuid = ParamUtil.getString(resourceRequest, "uuid");
			
			writeJSON(resourceRequest, resourceResponse, DocumentUtil.getDLFileContent(themeDisplay, uuid));
		
		} else if (ArrayUtil.contains(DocumentUtil.MIME_TYPES_CONTENT_DISPOSITION_INLINE, FileUtil.getExtension(resourceId)) && isDocument) {
			
			String uuid = ParamUtil.getString(resourceRequest, "uuid");
			
			InputStream inputStream = new ByteArrayInputStream(Base64.encode(DocumentUtil.getDLFileBytes(themeDisplay, uuid)).getBytes(StandardCharsets.UTF_8));
			
			ServletResponseUtil.sendFile(PortalUtil.getHttpServletRequest(resourceRequest), PortalUtil.getHttpServletResponse(resourceResponse), resourceId, inputStream);
			
			/*JSONObject base64JSONObject = JSONFactoryUtil.createJSONObject();
			base64JSONObject.put("content", Base64.encode(DocumentUtil.getDLFileBytes(themeDisplay, uuid)));
			
			writeJSON(resourceRequest, resourceResponse, base64JSONObject);*/
		}
		
		super.serveResource(resourceRequest, resourceResponse);
	}
	
	public void saveResourceImporter(ActionRequest actionRequest,
			ActionResponse actionResponse) {
		
		PortletPreferences portletPreferences = actionRequest.getPreferences();
		
		String resourceImporterURL = PrefsParamUtil.getString(portletPreferences, actionRequest, "resourceImporterURL");
		String pluginSourceURL = PrefsParamUtil.getString(portletPreferences, actionRequest, "pluginSourceURL");

		try {
			portletPreferences.setValue("resourceImporterURL", resourceImporterURL);
			portletPreferences.setValue("pluginSourceURL", pluginSourceURL);
			
			portletPreferences.store();
			
		} catch (ReadOnlyException | ValidatorException | IOException e) {
			_log.error(e.getMessage());
		}
		
		String fullPathResourceImporterURL = AutoDeployUtil.getAutoDeployDestDir() + StringPool.SLASH + resourceImporterURL + "/resources-importer/";
		String fullPathPluginSourceURL = pluginSourceURL + "/docroot/WEB-INF/src/resources-importer/";
		
		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

		//writting tree with flag writeFlag set to true
		TreeUtil.buildJSONTree(themeDisplay, true, fullPathResourceImporterURL, fullPathPluginSourceURL);
	}

	private static Log _log = LogFactoryUtil.getLog(SitemapGeneratorPortlet.class);
}
