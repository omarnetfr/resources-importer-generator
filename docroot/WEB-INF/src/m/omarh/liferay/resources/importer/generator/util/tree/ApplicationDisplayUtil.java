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
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.LayoutSet;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetTag;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.portlet.wiki.model.WikiPage;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class ApplicationDisplayUtil {
	
	public static JSONObject buildJSONApplicationDisplay (Group scopeGroup, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONArray applicationDisplaysJSONArray = JSONFactoryUtil.createJSONArray();
		
		try {
			for (Object[] applicationDisplayTemplateType : _APPLICATION_DISPLAY_TEMPLATE_TYPES) {

				Class<?> clazz = (Class<?>)applicationDisplayTemplateType[1];

				List<DDMTemplate> applicationDisplays = DDMTemplateLocalServiceUtil.getTemplates(scopeGroup.getGroupId(), PortalUtil.getClassNameId(clazz));
				
				if (applicationDisplays.size() == 0) {
					continue;
				}
				
				JSONArray applicationDisplayFolderChildrenJSONObject = JSONFactoryUtil.createJSONArray();
		
				for (DDMTemplate applicationDisplay : applicationDisplays) {
					
					JSONObject applicationDisplayJSONObject = DDMTemplateUtil.createDDMTemplateJSONObject(applicationDisplay, writeFile, fullPathResourceImporterURL + String.valueOf(applicationDisplayTemplateType[0]) + "/", fullPathPluginSourceURL + String.valueOf(applicationDisplayTemplateType[0]) + "/");
					
					applicationDisplayFolderChildrenJSONObject.put(applicationDisplayJSONObject);
				}
				
				JSONObject applicationDisplayFolderJSONObject = JSONFactoryUtil.createJSONObject();
				
				applicationDisplayFolderJSONObject.put("name", String.valueOf(applicationDisplayTemplateType[0]) + "/");
				applicationDisplayFolderJSONObject.put("children", applicationDisplayFolderChildrenJSONObject);
				applicationDisplayFolderJSONObject.put("total", applicationDisplayFolderChildrenJSONObject.length());
				
				applicationDisplaysJSONArray.put(applicationDisplayFolderJSONObject);
			}
			
		} catch (SystemException e) {
			if (_log.isErrorEnabled()) {
				_log.error(e);
			}
		}
		
		JSONObject applicationDisplayJSONObject = JSONFactoryUtil.createJSONObject();
		
		applicationDisplayJSONObject.put("children", applicationDisplaysJSONArray);
		applicationDisplayJSONObject.put("total", applicationDisplaysJSONArray.length());
		
		return applicationDisplayJSONObject;
	}
		
	private static final Object[][] _APPLICATION_DISPLAY_TEMPLATE_TYPES =
		new Object[][] {
			{"asset_category", AssetCategory.class},
			{"asset_entry", AssetEntry.class}, {"asset_tag", AssetTag.class},
			{"blogs_entry", BlogsEntry.class},
			{"document_library",FileEntry.class}, {"site_map", LayoutSet.class},
			{"wiki_page", WikiPage.class}
		};
	
	private static Log _log = LogFactoryUtil.getLog(ApplicationDisplayUtil.class);
}
