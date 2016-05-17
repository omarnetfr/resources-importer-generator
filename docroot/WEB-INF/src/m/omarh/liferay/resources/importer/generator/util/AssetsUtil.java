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

import java.util.List;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.asset.service.persistence.AssetEntryQuery;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.journal.model.JournalArticle;

public class AssetsUtil {
	
	public static JSONObject generateJSONAsset(Group scopeGroup) {
		
		JSONArray jsonAssetsArray = JSONFactoryUtil.createJSONArray();
		
		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();
		
		assetEntryQuery.setClassNameIds(new long[]{PortalUtil.getClassNameId(JournalArticle.class), PortalUtil.getClassNameId(DLFileEntry.class)});
		assetEntryQuery.setGroupIds(new long[]{scopeGroup.getGroupId()});
		
		try {
			List<AssetEntry> entries = AssetEntryLocalServiceUtil.getEntries(assetEntryQuery);
			
			for (AssetEntry assetEntry : entries) {
				JSONObject assetObject = JSONFactoryUtil.createJSONObject();
				
				//TODO: support of small image
				if (Validator.isNull(assetEntry.getSummaryCurrentValue()) &&
					ArrayUtil.isEmpty(assetEntry.getTagNames())) {
					continue;
				}
				
				if (Validator.isNotNull(assetEntry.getSummaryCurrentValue())) {
					assetObject.put("abstractSummary", assetEntry.getSummaryCurrentValue());
				}
				assetObject.put("name", assetEntry.getTitleCurrentValue());
				//assetObject.put("smallImage", value);
				
				if (ArrayUtil.isNotEmpty(assetEntry.getTagNames())) {
					
					JSONArray tagJSONArray = JSONFactoryUtil.createJSONArray();
					
					for (String tag : assetEntry.getTagNames()) {
						tagJSONArray.put(tag);
					}
					
					assetObject.put("tags", tagJSONArray);
				}
				
				jsonAssetsArray.put(assetObject);
			}
			
		} catch (SystemException e) {
			_log.error(e.getMessage());
		}

		JSONObject assetObject = JSONFactoryUtil.createJSONObject();
		assetObject.put("assets", jsonAssetsArray);
		
		return assetObject;
	}
	
	private static final Log _log = LogFactoryUtil.getLog(AssetsUtil.class);
}
