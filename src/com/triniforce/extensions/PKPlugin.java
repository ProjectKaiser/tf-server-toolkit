/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;


public class PKPlugin {
    
    IPKRootExtensionPoint m_rep;
    private String m_providerName = "";
    
    public PKPlugin() {
    }

    public IPKRootExtensionPoint getRootExtensionPoint() {
        return m_rep;
    }

    public void setRootExtensionPoint(IPKRootExtensionPoint rep) {
        m_rep = rep;
    }

    public String getProviderName() {
        return m_providerName;
    }

    void setAttrs(IPKExtensionBase e) {
        e.setPluginId(this.getClass().getName());
    }

    public void putExtensionPoint(IPKExtensionPoint ep){
        setAttrs(ep);
        m_rep.putExtensionPoint(ep);
    }
    
    public void putExtension(Class extensionPointClass, Class cls) {
        IPKExtension e = m_rep.getExtensionPoint(extensionPointClass).putExtension(cls);
        setAttrs(e);
    }

	public void setProviderName(String providerName) {
		m_providerName = providerName;
	}
	
	public void setVersion(String version) {
		m_version = version;
	}

	public String getVersion() {
		return m_version;
	}

	private String m_version = "";
	
	public void doRegistration(){}
	
}
