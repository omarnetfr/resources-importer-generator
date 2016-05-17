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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ClassResolverUtil;
import com.liferay.portal.kernel.util.MethodKey;
import com.liferay.portal.kernel.util.PortalClassInvoker;

/**
 * 
 * @author Omar HADDOUCHI
 * 
 */
public class AutoDeployUtil {
	
	public static String getAutoDeployDestDir() {
		
		try {
			return (String) PortalClassInvoker.invoke(false, _getAutoDeployDirMethodKey);
		} catch (Exception e) {
			_log.error(e.getMessage());
		}
		
		return "";
	}
	
	private static MethodKey _getAutoDeployDirMethodKey = new MethodKey(
			ClassResolverUtil.resolveByPortalClassLoader("com.liferay.portal.deploy.DeployUtil"),
			"getAutoDeployDestDir");
	
	private static final Log _log = LogFactoryUtil.getLog(AutoDeployUtil.class); 
}
