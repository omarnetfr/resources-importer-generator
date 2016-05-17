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
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class JournalStructureUtil {
	
	public static JSONObject buildJSONDDMStrcutures (Group scopeGroup, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONArray journalStrucutresJSONArray = JSONFactoryUtil.createJSONArray();
		
		try {
			List<DDMStructure> ddmStructures = DDMStructureLocalServiceUtil.getStructures(scopeGroup.getGroupId(), PortalUtil.getClassNameId(JournalArticle.class), -1, -1);
		
			for (DDMStructure ddmStructure : ddmStructures) {
				
				if (Validator.isNull(ddmStructure.getParentStructureId())) {
					
					JSONObject ddmStructureJSONObject = createDDMStructureJSONObject(ddmStructure, writeFile, fullPathResourceImporterURL, fullPathPluginSourceURL);
					
					journalStrucutresJSONArray.put(ddmStructureJSONObject);
					
				} else {
					
					String parentStructureName = getStructureName(scopeGroup, ddmStructure.getParentStructureId());
					
					JSONObject parentStructureFolderJSONObject = JSONUtil.getJSONObjectByName(journalStrucutresJSONArray, parentStructureName + "/");
					
					if (Validator.isNull(parentStructureFolderJSONObject)) {
						
						JSONArray articleFolderChildrenJSONArray = JSONFactoryUtil.createJSONArray();
						
						parentStructureFolderJSONObject = JSONFactoryUtil.createJSONObject();

						parentStructureFolderJSONObject.put("name", parentStructureName + "/");
						parentStructureFolderJSONObject.put("children", articleFolderChildrenJSONArray);
						
						journalStrucutresJSONArray.put(parentStructureFolderJSONObject);
					}

					JSONObject journalStrucutreJSONObject = createDDMStructureJSONObject(ddmStructure, writeFile, fullPathResourceImporterURL + parentStructureName + "/", fullPathPluginSourceURL + parentStructureName + "/");
					
					parentStructureFolderJSONObject.getJSONArray("children").put(journalStrucutreJSONObject);
				}
			}
			
		} catch (SystemException e) {
			if (_log.isErrorEnabled()) {
				_log.error(e);
			}
		}
		
		JSONObject journalStrucutresJSONObject = JSONFactoryUtil.createJSONObject();
		
		journalStrucutresJSONObject.put("children", journalStrucutresJSONArray);
		journalStrucutresJSONObject.put("total", journalStrucutresJSONArray.length());
		
		return journalStrucutresJSONObject;
	}
	
	public static String getStructureName(Group scopeGroup, long structureId) throws SystemException {
		
		String structureName = String.valueOf(structureId);
		
		DDMStructure ddmStructure = DDMStructureLocalServiceUtil.fetchStructure(structureId);

		structureName = ddmStructure.getNameCurrentValue();
		
		return structureName;
	}
	
	public static String getStructureName(Group scopeGroup, String structureKey) throws SystemException {
		
		String structureName = structureKey;
		
		try {
			DDMStructure ddmStructure = DDMStructureLocalServiceUtil.fetchStructure(scopeGroup.getGroupId(), PortalUtil.getClassNameId(JournalArticle.class), structureKey, true);
		
			structureName = ddmStructure.getNameCurrentValue();
			
		} catch (PortalException e) {
			_log.error(e.getMessage());
		}
		
		return structureName;
	}
	
	public static Object getJournalStructureContent(ThemeDisplay themeDisplay, String uuid) {
			
		JSONObject htmlJSONObject = JSONFactoryUtil.createJSONObject();

		try {	
			String ddmStructureContent = getDDMStructureContent(themeDisplay.getScopeGroupId(),
					uuid);

			htmlJSONObject.put("content", ddmStructureContent);
			
		} catch (SystemException e) {
			_log.error(e.getMessage());
		}
		
		return htmlJSONObject;
	}

	private static String getDDMStructureContent(
			long groupId, String uuid) throws SystemException {
		
		DDMStructure ddmStructure = DDMStructureLocalServiceUtil.fetchDDMStructureByUuidAndGroupId(uuid, groupId);
		
		return ddmStructure.getXsd();
	}
	
	private static JSONObject createDDMStructureJSONObject(
			DDMStructure ddmStructure, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONObject ddmStructureJSONObject = JSONFactoryUtil.createJSONObject();
		
		String fileName = ddmStructure.getNameCurrentValue() + ".xml";
		
		ddmStructureJSONObject.put("name", fileName);
		ddmStructureJSONObject.put("uuid", ddmStructure.getUuid());
		ddmStructureJSONObject.put("leaf", true);
		
		if (writeFile) {

			try {
				String fileContent = getDDMStructureContent(ddmStructure.getGroupId(), ddmStructure.getUuid());
				
				JSONUtil.writeJSONObjectToFile(fullPathResourceImporterURL + fileName, fileContent);
				JSONUtil.writeJSONObjectToFile(fullPathPluginSourceURL + fileName, fileContent);
				
			} catch (SystemException e) {
				_log.error(e.getMessage());
			}
			
			
		}
		
		return ddmStructureJSONObject;
	}
	
	private static Log _log = LogFactoryUtil.getLog(JournalStructureUtil.class);
}
