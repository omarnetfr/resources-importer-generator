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

import m.omarh.liferay.resources.importer.generator.util.DDMTemplateUtil;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class JournalTemplateUtil {

	public static JSONObject buildJSONDDMTemplates(Group scopeGroup, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONArray journalTemplatesJSONArray = JSONFactoryUtil.createJSONArray();
		
		try {
			List<DDMStructure> ddmStrucutres = DDMStructureLocalServiceUtil.getStructures(scopeGroup.getGroupId(), PortalUtil.getClassNameId(JournalArticle.class), -1, -1);
		
			for (DDMStructure ddmStructure : ddmStrucutres) {
				
				if (Validator.isNotNull(ddmStructure.getTemplates()) && ddmStructure.getTemplates().size() > 0) {
					
					JSONArray journalTemplateFolderChildrenJSONArray = JSONFactoryUtil.createJSONArray();
					
					for (DDMTemplate ddmTemplate : ddmStructure.getTemplates()) {
						journalTemplateFolderChildrenJSONArray.put(DDMTemplateUtil.createDDMTemplateJSONObject(ddmTemplate, writeFile, fullPathResourceImporterURL + ddmStructure.getNameCurrentValue() + "/", fullPathPluginSourceURL + ddmStructure.getNameCurrentValue() + "/"));
					}
					
					JSONObject journalTemplateFolderJSONObject = JSONFactoryUtil.createJSONObject();
					
					journalTemplateFolderJSONObject.put("name", ddmStructure.getNameCurrentValue() + "/");
					journalTemplateFolderJSONObject.put("children", journalTemplateFolderChildrenJSONArray);
					
					journalTemplatesJSONArray.put(journalTemplateFolderJSONObject);
				}
			}
			
		} catch (SystemException e) {
			if (_log.isErrorEnabled()) {
				_log.error(e);
			}
		}
		
		JSONObject journalTemplatesJSONObject = JSONFactoryUtil.createJSONObject();
		
		journalTemplatesJSONObject.put("children", journalTemplatesJSONArray);
		journalTemplatesJSONObject.put("total", journalTemplatesJSONArray.length());
		
		return journalTemplatesJSONObject;
	}
	
	public static JSONObject getJournalTemplateContent(ThemeDisplay themeDisplay, String uuid) {
		
		JSONObject htmlJSONObject = JSONFactoryUtil.createJSONObject();

		try {	
			String ddmTemplateContent = DDMTemplateUtil.getDDMTemplateContent(themeDisplay.getScopeGroupId(), uuid);

			htmlJSONObject.put("content", ddmTemplateContent);
			
		} catch (SystemException e) {
			_log.error(e.getMessage());
		}
		
		return htmlJSONObject;
	}

	private static Log _log = LogFactoryUtil.getLog(JournalTemplateUtil.class);
}
