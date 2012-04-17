/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;

import java.util.LinkedHashMap;
import java.util.Map;

import com.triniforce.utils.ApiAlgs;

public class PKRootExtensionPoint extends PKExtensionBase implements
		IPKRootExtensionPoint {

	Map<String, IPKExtensionPoint> m_extensionPoints = new LinkedHashMap<String, IPKExtensionPoint>();
	
    public Map<String, IPKExtensionPoint> getExtensionPoints() {
        return m_extensionPoints;
    }
	
	public PKRootExtensionPoint() {
		getEpPlugins();
		getEpFunctions();
	}

	public IPKExtension getExtension(String extensionPointId, String extensionId)
			throws EExtensionPointNotFound, EExtensionNotFound {
		return getExtensionPoint(extensionPointId).getExtension(extensionId);
	}

	public IPKExtension getExtension(Class extensionPointClass,
			Class extensionClass) throws EExtensionPointNotFound,
			EExtensionNotFound {
		return getExtensionPoint(extensionPointClass).getExtension(
				extensionClass);
	}
	
	public IPKExtensionPoint getExtensionPoint(Class extensionPointClass)
			throws EExtensionPointNotFound {
		return getExtensionPoint(extensionPointClass.getName());
	}
	
	public IPKExtensionPoint getExtensionPoint(String extensionPointId)
			throws EExtensionPointNotFound {
		IPKExtensionPoint ep = m_extensionPoints.get(extensionPointId);
		if (null == ep)
			throw new EExtensionPointNotFound(extensionPointId, this);
		return ep;
	}

	IPKExtensionPoint getPredefinedEp(Class cls) {
		IPKExtensionPoint res = getExtensionPoints().get(cls.getName());
		if (null == res) {
			try {
				res = (IPKExtensionPoint) cls.newInstance();
				putExtensionPoint(res);
			} catch (Exception e) {
				ApiAlgs.rethrowException(e);
			}
		}
		return res;
	}

	public IPKExtensionPoint getEpPlugins() {
		return getPredefinedEp(PKEPPlugins.class);
	}

	public IPKExtensionPoint getEpFunctions() {
		return getPredefinedEp(PKEPFunctions.class);
	}

	public void putExtensionPoint(String extensionPointId, IPKExtensionPoint ep) {
		m_extensionPoints.put(extensionPointId, ep);
		ep.setId(extensionPointId);
		ep.setRootExtensionPoint(this);
	}

	public void putExtensionPoint(IPKExtensionPoint ep) {
		putExtensionPoint(ep.getClass().getName(), ep);
	}

}
