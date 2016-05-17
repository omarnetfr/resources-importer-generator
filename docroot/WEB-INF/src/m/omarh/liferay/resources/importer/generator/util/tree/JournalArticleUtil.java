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

import java.util.List;

import m.omarh.liferay.resources.importer.generator.util.JSONUtil;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.journalcontent.util.JournalContentUtil;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class JournalArticleUtil {
	
	public static JSONObject buildJSONJournalArticles (ThemeDisplay themeDisplay, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONArray articlesJSONArray = JSONFactoryUtil.createJSONArray();
		
		try {
			List<JournalArticle> journalArticles = JournalArticleLocalServiceUtil.search(themeDisplay.getScopeGroupId(), 0, WorkflowConstants.STATUS_APPROVED, -1, -1);
		
			for (JournalArticle journalArticle : journalArticles) {
				
				if (Validator.isNull(journalArticle.getStructureId())) {
					
					JSONObject articleJSONObject = createArticleJSONObject(journalArticle, themeDisplay, writeFile, fullPathResourceImporterURL, fullPathPluginSourceURL);
					
					articlesJSONArray.put(articleJSONObject);
					
				} else {
					
					String structureName = JournalStructureUtil.getStructureName(themeDisplay.getScopeGroup(), journalArticle.getStructureId());
					
					JSONObject articleFolderJSONObject = JSONUtil.getJSONObjectByName(articlesJSONArray, structureName + "/");
					
					if (Validator.isNull(articleFolderJSONObject)) {
						
						JSONArray articleFolderChildrenJSONArray = JSONFactoryUtil.createJSONArray();
						
						articleFolderJSONObject = JSONFactoryUtil.createJSONObject();

						articleFolderJSONObject.put("name", structureName + "/");
						articleFolderJSONObject.put("children", articleFolderChildrenJSONArray);
						
						articlesJSONArray.put(articleFolderJSONObject);
					}

					JSONObject articleJSONObject = createArticleJSONObject(journalArticle, themeDisplay, writeFile, fullPathResourceImporterURL + structureName + "/", fullPathPluginSourceURL + structureName + "/");
					
					articleFolderJSONObject.getJSONArray("children").put(articleJSONObject);
				}
			}
			
		} catch (SystemException e) {
			if (_log.isErrorEnabled()) {
				_log.error(e);
			}
		}
		
		JSONObject articlesJSONObject = JSONFactoryUtil.createJSONObject();
		
		articlesJSONObject.put("children", articlesJSONArray);
		articlesJSONObject.put("total", articlesJSONArray.length());
		
		return articlesJSONObject;
	}
	
	public static JSONObject getJournalArticleContentJSONObject(ThemeDisplay themeDisplay, String resourceId) {
		
		JSONObject htmlJSONObject = JSONFactoryUtil.createJSONObject();

		try {
			String name = resourceId.substring(0, resourceId.indexOf(StringPool.PERIOD));
			
			String content = getJournalArticleContent(themeDisplay, name);
			
			htmlJSONObject.put("content", content);
			
		} catch (PortalException e) {
			_log.error(e.getMessage());
		} catch (SystemException e) {
			_log.error(e.getMessage());
		}
		
		return htmlJSONObject;
	}

	private static String getJournalArticleContent(ThemeDisplay themeDisplay,
			String name) throws PortalException, SystemException {
		
		JournalArticle journalArticle = JournalArticleLocalServiceUtil.getDisplayArticleByUrlTitle(themeDisplay.getScopeGroupId(), FriendlyURLNormalizerUtil.normalize(name));
		
		String content = StringPool.BLANK;
		
		if (Validator.isNull(journalArticle.getStructureId())) {
			
			content = JournalContentUtil.getContent(
					journalArticle.getGroupId(), journalArticle.getArticleId(), Constants.VIEW, LocaleUtil.toLanguageId(LocaleUtil.getDefault()), themeDisplay);
			
		} else {
			
			content = journalArticle.getContent();
		}
		
		return content;
	}

	private static JSONObject createArticleJSONObject(
			JournalArticle journalArticle, ThemeDisplay themeDisplay, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONObject articleJSONObject = JSONFactoryUtil.createJSONObject();
		
		String fileName = journalArticle.getTitleCurrentValue() + ".html";
		
		articleJSONObject.put("name", fileName);
		articleJSONObject.put("leaf", true);
		
		if (writeFile) {
			
			try {
				String fileContent = getJournalArticleContent(themeDisplay, journalArticle.getUrlTitle());
				
				JSONUtil.writeJSONObjectToFile(fullPathResourceImporterURL + fileName, fileContent);
				JSONUtil.writeJSONObjectToFile(fullPathPluginSourceURL + fileName, fileContent);
			
			} catch (Exception e) {
				_log.error(e.getMessage());
			}
			
		}
		
		return articleJSONObject;
	}
		
	private static Log _log = LogFactoryUtil.getLog(JournalArticleUtil.class);
}
