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

import java.io.IOException;
import java.util.List;

import m.omarh.liferay.resources.importer.generator.util.JSONUtil;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class DocumentUtil {
	
	public static final long DEFAULT_PARENT_FOLDER_ID = 0;
	
	public static final String[] MIME_TYPES_CONTENT_DISPOSITION_INLINE = PropsUtil.getArray(PropsKeys.MIME_TYPES_CONTENT_DISPOSITION_INLINE);
	
	public static final String MIME_TYPES_WEB_IMAGES = PropsUtil.get(PropsKeys.MIME_TYPES_WEB_IMAGES);
	
	public static final String[] TEXT_EXTENSIONS = StringUtil.split("txt,properties,csv,c,cpp,asp,php,java,jsp,css,js,diff,md,ini,make,sql,py,bat,sh,ksh,bash,json,log,xml,html");
	
	public static JSONObject buildJSONDocuments (Group scopeGroup, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONArray documentsJSONArray = null;

		try {
			documentsJSONArray = getFolders(scopeGroup, DEFAULT_PARENT_FOLDER_ID, writeFile, fullPathResourceImporterURL, fullPathPluginSourceURL);
			
		} catch (SystemException e) {
			if (_log.isErrorEnabled()) {
				_log.error(e);
			}
		}
		
		JSONObject documentJSONObject = JSONFactoryUtil.createJSONObject();
		
		documentJSONObject.put("children", documentsJSONArray);
		documentJSONObject.put("total", documentsJSONArray.length());
		
		return documentJSONObject;
	}
	
	public static Object getDLFileContent(ThemeDisplay themeDisplay, String uuid) {

		JSONObject htmlJSONObject = JSONFactoryUtil.createJSONObject();

		try {
			
			DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.getFileEntryByUuidAndGroupId(uuid, themeDisplay.getScopeGroupId());

			String content = new String(FileUtil.getBytes(dlFileEntry.getContentStream()));

			htmlJSONObject.put("content", content);
			
		} catch (PortalException e) {
			_log.error(e.getMessage());
		} catch (SystemException e) {
			_log.error(e.getMessage());
		} catch (IOException e) {
			_log.error(e.getMessage());
		}
		
		return htmlJSONObject;
	}
	
	public static byte[] getDLFileBytes(ThemeDisplay themeDisplay, String uuid) {

		byte[] bytes = null;

		try {
			
			DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.getFileEntryByUuidAndGroupId(uuid, themeDisplay.getScopeGroupId());

			bytes = FileUtil.getBytes(dlFileEntry.getContentStream());
			
		} catch (PortalException e) {
			_log.error(e.getMessage());
		} catch (SystemException e) {
			_log.error(e.getMessage());
		} catch (IOException e) {
			_log.error(e.getMessage());
		}
		
		return bytes;
	}
	
	public static byte[] getDLFileBytes(long groupId, String uuid) {

		byte[] bytes = null;

		try {
			
			DLFileEntry dlFileEntry = DLFileEntryLocalServiceUtil.getFileEntryByUuidAndGroupId(uuid, groupId);

			bytes = FileUtil.getBytes(dlFileEntry.getContentStream());
			
		} catch (PortalException e) {
			_log.error(e.getMessage());
		} catch (SystemException e) {
			_log.error(e.getMessage());
		} catch (IOException e) {
			_log.error(e.getMessage());
		}
		
		return bytes;
	}

	private static JSONArray getFolders(Group scopeGroup, long folderId, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) throws SystemException {
		
		JSONArray documentsJSONArray = getDLFileEntries(scopeGroup, folderId, writeFile, fullPathResourceImporterURL, fullPathPluginSourceURL);
		
		List<DLFolder> dlFolders =  DLFolderLocalServiceUtil.getFolders(scopeGroup.getGroupId(), folderId, false);
		
		for (DLFolder dlFolder : dlFolders) {
			
			JSONObject dlFolderJSONObject = createDLFolderJSONObject(dlFolder);
			
			//JSONArray dlFileEntriesJSONArray = getDLFileEntries(scopeGroup, dlFolder.getFolderId());
			JSONArray dlFileEntriesJSONArray = getFolders(scopeGroup, dlFolder.getFolderId(), writeFile, fullPathResourceImporterURL + dlFolderJSONObject.getString("name"), fullPathPluginSourceURL + dlFolderJSONObject.getString("name"));
			
			dlFolderJSONObject.put("children", dlFileEntriesJSONArray);
			dlFolderJSONObject.put("total", dlFileEntriesJSONArray.length());
			
			documentsJSONArray.put(dlFolderJSONObject);
		}
		
		return documentsJSONArray;
	}

	private static JSONArray getDLFileEntries(Group scopeGroup, long folderId, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) throws SystemException {
		
		JSONArray documentsJSONArray = JSONFactoryUtil.createJSONArray();
		
		List<DLFileEntry> dlFileEntries = DLFileEntryLocalServiceUtil.getFileEntries(scopeGroup.getGroupId(), folderId);
		
		for (DLFileEntry dlFileEntry : dlFileEntries) {
			
			documentsJSONArray.put(createDLFileEntryJSONObject(dlFileEntry, writeFile, fullPathResourceImporterURL, fullPathPluginSourceURL));
		}
		
		return documentsJSONArray;
	}

	private static JSONObject createDLFileEntryJSONObject(
			DLFileEntry dlFileEntry, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {
		
		JSONObject dlFileEntryJSONObject = JSONFactoryUtil.createJSONObject();
		
		String fileName = dlFileEntry.getTitle();
		
		if (Validator.isNull(FileUtil.getExtension(fileName))) {
			fileName += "." + dlFileEntry.getExtension();
		}
		
		dlFileEntryJSONObject.put("name", fileName);
		dlFileEntryJSONObject.put("uuid", dlFileEntry.getUuid());
		dlFileEntryJSONObject.put("document", true);
		dlFileEntryJSONObject.put("leaf", true);
		
		if (writeFile) {
			
			byte[] fileContent = getDLFileBytes(dlFileEntry.getGroupId(), dlFileEntry.getUuid());
			
			JSONUtil.writeJSONObjectToFile(fullPathResourceImporterURL + fileName, fileContent);
			JSONUtil.writeJSONObjectToFile(fullPathPluginSourceURL + fileName, fileContent);
		}
		
		return dlFileEntryJSONObject;
	}
	
	private static JSONObject createDLFolderJSONObject(
			DLFolder dlFolder) {
		
		JSONObject dlFolderJSONObject = JSONFactoryUtil.createJSONObject();
		
		dlFolderJSONObject.put("name", dlFolder.getName() + "/");
		
		return dlFolderJSONObject;
	}

	private static Log _log = LogFactoryUtil.getLog(DocumentUtil.class);
}
