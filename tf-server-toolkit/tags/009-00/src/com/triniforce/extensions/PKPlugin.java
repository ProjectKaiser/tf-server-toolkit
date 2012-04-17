/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;


public abstract class PKPlugin implements IPlugin {
    
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
    
    public void putExtension(Class extensionPointClass, String extensionId, Object obj) {
        IPKExtension e = m_rep.getExtensionPoint(extensionPointClass).putExtension(extensionId, obj);
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
	
	public abstract void doRegistration();

    public abstract void doExtensionPointsRegistration();

    public void doRegistration(ISORegistration reg) throws EDBObjectException {
    }

    public void finit() {
    }

    public String[] getDependencies() {
        return new String[]{};
    }

    public String getPluginName() {
        return "";
    }

    public void init() {
    }

    public void popApi(Mode mode, ApiStack stk) {
    }

    public void prepareApi() {
    }

    public void pushApi(Mode mode, ApiStack apiStack) {
    }
	
}
