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

import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.portlet.PortletPreferences;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class SiteMapUtil {
	private static final Pattern PATTERN = Pattern.compile("^column-[1-9]$");
	
	public static JSONObject generateJSONSitemap(Group scopeGroup) {
		
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		Layout groupLayout = null;
		
		try {
			groupLayout = LayoutLocalServiceUtil.getLayout(scopeGroup.getDefaultPublicPlid());
		} catch (PortalException e) {
			_log.error(e.getMessage());
		} catch (SystemException e) {
			_log.error(e.getMessage());
		}
		
		LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet)groupLayout.getLayoutType();
		
		jsonObject.put("themeId", groupLayout.getThemeId());
		jsonObject.put("layoutTemplateId", layoutTypePortlet.getLayoutTemplateId());

		if (scopeGroup.hasPrivateLayouts()) {

			jsonObject.put("privatePages", buildLayouts(scopeGroup, true));
		}
		
		if (scopeGroup.hasPublicLayouts()) {

			jsonObject.put("publicPages", buildLayouts(scopeGroup, false));
		}
		
		return jsonObject;
	}

	private static JSONArray buildLayouts(Group scopeGroup, boolean isPrivate) {
		
		JSONArray jsonArrayLayouts = JSONFactoryUtil.createJSONArray();
		
		try {
			for (Layout layout : LayoutLocalServiceUtil.getLayouts(scopeGroup.getGroupId(), isPrivate)) {
				
				jsonArrayLayouts.put(buildLayout(layout));
			}
		} catch (Exception e) {
			_log.error(e, e);
		}
		
		return jsonArrayLayouts;
	}

	private static JSONObject buildLayout(Layout layout) throws SystemException {
		
		JSONObject layoutJsonObject = JSONFactoryUtil.createJSONObject();
		
		JSONArray columnsJsonArray = JSONFactoryUtil.createJSONArray();
		
		UnicodeProperties unicodeProperties = layout.getTypeSettingsProperties();
		
		for(String key : unicodeProperties.keySet()){
			
			if (key.equals("layout-template-id")) {
				
				layoutJsonObject.put("layoutTemplateId", unicodeProperties.getProperty(key));
			}
			
			if (PATTERN.matcher(key).matches()) {
				JSONArray portletsJsonArray = JSONFactoryUtil.createJSONArray();
				
				for (String portletId : StringUtil.split(unicodeProperties.getProperty(key))) {
					
					PortletPreferences portletPreferences =
						PortletPreferencesFactoryUtil.getLayoutPortletSetup(
							layout, portletId);

					if (portletId.equals(PortletKeys.NESTED_PORTLETS)) {
						//TODO: nested portlet
					} else if (portletId.startsWith(PortletKeys.JOURNAL_CONTENT)) {
						//TODO: journal portlet html
						String articleId = portletPreferences.getValue("articleId", "");
						
						try {
							JournalArticle journalArticle = JournalArticleLocalServiceUtil.getArticle(layout.getGroupId(), articleId);
							
							portletsJsonArray.put(journalArticle.getTitle(LocaleUtil.getDefault()) + ".html");
							
						} catch (PortalException e) {
							_log.error(e, e);
						}
						
					} else {
						
						JSONObject portletPreferencesJsonObject = JSONFactoryUtil.createJSONObject();
						
						for (Entry<String, String[]> entry : portletPreferences.getMap().entrySet()) {
						    portletPreferencesJsonObject.put(entry.getKey(), entry.getValue()[0]);
						}
						
						JSONObject portletJsonObject = JSONFactoryUtil.createJSONObject();
						portletJsonObject.put("portletId", portletId);
						
						if (!portletPreferences.getMap().isEmpty()) {
							portletJsonObject.put("portletPreferences", portletPreferencesJsonObject);
						}
						
						portletsJsonArray.put(portletJsonObject);
					}
				}
				
				columnsJsonArray.put(portletsJsonArray);
			}
		}
		
		if (layout.hasChildren()) {
			JSONArray layoutChildrenJsonArray = JSONFactoryUtil.createJSONArray();
			
			for (Layout childLayout : layout.getAllChildren()) {
				layoutChildrenJsonArray.put(buildLayout(childLayout));
			}
			
			layoutJsonObject.put("children", layoutChildrenJsonArray);
		}
		
		if (columnsJsonArray.length() > 0) {
			layoutJsonObject.put("columns", columnsJsonArray);
		}
		layoutJsonObject.put("name", layout.getName(LocaleUtil.getDefault()));
		layoutJsonObject.put("friendlyURL", layout.getFriendlyURL());
		layoutJsonObject.put("title", layout.getTitle(LocaleUtil.getDefault()));
		layoutJsonObject.put("hidden", String.valueOf(layout.isHidden()));
		
		return layoutJsonObject;
	}

	private static final Log _log = LogFactoryUtil.getLog(SiteMapUtil.class); 
}
