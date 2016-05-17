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
package m.omarh.liferay.resources.importer.generator.util.tree;

import m.omarh.liferay.resources.importer.generator.util.AssetsUtil;
import m.omarh.liferay.resources.importer.generator.util.JSONUtil;
import m.omarh.liferay.resources.importer.generator.util.SiteMapUtil;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.theme.ThemeDisplay;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class TreeUtil {

	public static JSONObject buildJSONTree(ThemeDisplay themeDisplay, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		//sitemap.json
		JSONObject sitemapJSONObject = JSONFactoryUtil.createJSONObject();
		sitemapJSONObject.put("name", "sitemap.json");
		sitemapJSONObject.put("leaf", true);
		
		if (writeFile) {
			//writting of sitemap.json
			String sitemap = JSONUtil.valorizeVariables(themeDisplay, SiteMapUtil.generateJSONSitemap(themeDisplay.getScopeGroup()).toString());
			
			JSONUtil.writeJSONObjectToFile(fullPathResourceImporterURL + "/sitemap.json", sitemap);
			JSONUtil.writeJSONObjectToFile(fullPathPluginSourceURL + "/sitemap.json", sitemap);
		}
		
		//asset.json
		JSONObject assetJSONObject = JSONFactoryUtil.createJSONObject();
		assetJSONObject.put("name", "assets.json");
		assetJSONObject.put("leaf", true);
		
		if (writeFile) {
			//writting of assets.json
			String assets = JSONUtil.valorizeVariables(themeDisplay, AssetsUtil.generateJSONAsset(themeDisplay.getScopeGroup()).toString());
			
			JSONUtil.writeJSONObjectToFile(fullPathResourceImporterURL + "/assets.json", assets);
			JSONUtil.writeJSONObjectToFile(fullPathPluginSourceURL + "/assets.json", assets);
		}
		
		//documents/
		JSONObject documentsJSONObject = JSONFactoryUtil.createJSONObject();
		documentsJSONObject.put("name", "documents/");
		
		if (writeFile) {
			DocumentUtil.buildJSONDocuments(themeDisplay.getScopeGroup(), true, fullPathResourceImporterURL + "document_library/documents/", fullPathPluginSourceURL + "document_library/documents/");
		}

		//documents/ children
		JSONArray documentLibraryChildrenJSONArray = JSONFactoryUtil.createJSONArray();
		documentLibraryChildrenJSONArray.put(documentsJSONObject);
		
		//document_library/
		JSONObject documentLibraryJSONObject = JSONFactoryUtil.createJSONObject();
		documentLibraryJSONObject.put("name", "document_library/");
		documentLibraryJSONObject.put("children", documentLibraryChildrenJSONArray);
		
		//articles/
		JSONObject articlesJSONObject = JSONFactoryUtil.createJSONObject();
		articlesJSONObject.put("name", "articles/");
		
		//structures/
		JSONObject structuresJSONObject = JSONFactoryUtil.createJSONObject();
		structuresJSONObject.put("name", "structures/");
		
		//templates/
		JSONObject templatesJSONObject = JSONFactoryUtil.createJSONObject();
		templatesJSONObject.put("name", "templates/");
		
		if (writeFile) {
			
			JournalArticleUtil.buildJSONJournalArticles(themeDisplay, true, fullPathResourceImporterURL + "journal/articles/", fullPathPluginSourceURL + "journal/articles/");
			
			JournalStructureUtil.buildJSONDDMStrcutures(themeDisplay.getScopeGroup(), true, fullPathResourceImporterURL + "journal/structures/", fullPathPluginSourceURL + "journal/structures/");
			
			JournalTemplateUtil.buildJSONDDMTemplates(themeDisplay.getScopeGroup(), true, fullPathResourceImporterURL + "journal/templates/", fullPathPluginSourceURL + "journal/templates/");
		}
		
		//journal/ children
		JSONArray journalChildrenJSONArray = JSONFactoryUtil.createJSONArray();
		journalChildrenJSONArray.put(articlesJSONObject);
		journalChildrenJSONArray.put(structuresJSONObject);
		journalChildrenJSONArray.put(templatesJSONObject);
		
		//journal/
		JSONObject journalJSONObject = JSONFactoryUtil.createJSONObject();
		journalJSONObject.put("name", "journal/");
		journalJSONObject.put("children", journalChildrenJSONArray);
		
		//structures/
		JSONObject articleDisplayJSONObject = JSONFactoryUtil.createJSONObject();
		articleDisplayJSONObject.put("name", "application_display/");
		
		if (writeFile) {
			ApplicationDisplayUtil.buildJSONApplicationDisplay(themeDisplay.getScopeGroup(), true, fullPathResourceImporterURL + "templates/application_display/", fullPathPluginSourceURL + "templates/application_display/");
		}
				
		//templates/ children 
		
		JSONArray templateChildrenJSONArray = JSONFactoryUtil.createJSONArray();
		templateChildrenJSONArray.put(articleDisplayJSONObject);
		
		// templates
		JSONObject templateJSONObject = JSONFactoryUtil.createJSONObject();
		templateJSONObject.put("name", "templates/");
		templateJSONObject.put("children", templateChildrenJSONArray);
				
		//tree children
		JSONArray jsonTreeChildrenArray = JSONFactoryUtil.createJSONArray();
		
		jsonTreeChildrenArray.put(sitemapJSONObject);
		jsonTreeChildrenArray.put(assetJSONObject);
		jsonTreeChildrenArray.put(documentLibraryJSONObject);
		jsonTreeChildrenArray.put(journalJSONObject);
		jsonTreeChildrenArray.put(templateJSONObject);
		
		//tree
		JSONObject treeJSONObject = JSONFactoryUtil.createJSONObject();
		treeJSONObject.put("children", jsonTreeChildrenArray);
		
		return treeJSONObject;
	}

	//private static Log _log = LogFactoryUtil.getLog(TreeUtil.class);
}
