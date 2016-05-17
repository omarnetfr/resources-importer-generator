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
package m.omarh.liferay.resources.importer.generator.util;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portlet.dynamicdatamapping.model.DDMTemplate;
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class DDMTemplateUtil {

	public static JSONObject createDDMTemplateJSONObject(
			DDMTemplate ddmTemplate, boolean writeFile, String fullPathResourceImporterURL, String fullPathPluginSourceURL) {

		JSONObject ddmTemplateJSONObject = JSONFactoryUtil.createJSONObject();
		
		String fileName = ddmTemplate.getNameCurrentValue() + "." + ddmTemplate.getLanguage();
		
		ddmTemplateJSONObject.put("name", fileName);
		ddmTemplateJSONObject.put("uuid", ddmTemplate.getUuid());
		ddmTemplateJSONObject.put("leaf", true);
		
		if (writeFile) {

			try {
				String fileContent = getDDMTemplateContent(ddmTemplate.getGroupId(), ddmTemplate.getUuid());
				
				JSONUtil.writeJSONObjectToFile(fullPathResourceImporterURL + fileName, fileContent);
				JSONUtil.writeJSONObjectToFile(fullPathPluginSourceURL + fileName, fileContent);
				
			} catch (SystemException e) {
				_log.error(e.getMessage());
			}
			
			
		}
		
		return ddmTemplateJSONObject;
	}
	
	public static String getDDMTemplateContent(long groupId,
			String uuid) throws SystemException {
		DDMTemplate ddmTemplate = DDMTemplateLocalServiceUtil.fetchDDMTemplateByUuidAndGroupId(uuid, groupId);
		return ddmTemplate.getScript();
	}
	
	private static Log _log = LogFactoryUtil.getLog(DDMTemplateUtil.class);
}
