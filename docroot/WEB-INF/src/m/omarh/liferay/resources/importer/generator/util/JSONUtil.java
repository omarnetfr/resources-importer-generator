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

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.theme.ThemeDisplay;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class JSONUtil {
	
	public static String beautify(String json) throws IOException {
	    ObjectMapper mapper = new ObjectMapper();
	    Object obj = mapper.readValue(json, Object.class);
	    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
	}
	
	public static String valorizeVariables(ThemeDisplay themeDisplay, String json) {
		
		String sitemap = StringUtil.replace(
			json,
			new String[] {
				String.valueOf(themeDisplay.getCompanyId()), String.valueOf(themeDisplay.getScopeGroupId()),
				String.valueOf(themeDisplay.getUserId())
			}, new String[] {"${companyId}", "${groupId}", "${userId}"});
		
		return sitemap;
	}
	
	public static JSONObject getJSONObjectByName(JSONArray articlesJSONArray, String name) {
		
		for (int i = 0; i < articlesJSONArray.length(); i++) {
			JSONObject articleJSONObject = articlesJSONArray.getJSONObject(i);
			
			if (articleJSONObject.getString("name").equals(name)) {
				return articleJSONObject;
			}
		}
		
		return null;
	}
	
	public static void writeJSONObjectToFile(String jsonObjectFileTarget,
			String sitemapJsonObjectString) {
		
		try {
			FileUtil.write(jsonObjectFileTarget, sitemapJsonObjectString);
		} catch (IOException e) {
			_log.error(e.getMessage());
		}
	}
	
	public static void writeJSONObjectToFile(String jsonObjectFileTarget,
			byte[] bytes) {
		
		try {
			FileUtil.write(jsonObjectFileTarget, bytes);
		} catch (IOException e) {
			_log.error(e.getMessage());
		}
	}
	
	private static Log _log = LogFactoryUtil.getLog(JSONUtil.class);
}
