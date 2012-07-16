/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import java.util.LinkedHashMap;
import java.util.Map;

public class PKExtensionPoint extends PKExtensionBase implements IPKExtensionPoint{

	Class m_extensionClass;
	
    public Class getExtensionClass() {
		return m_extensionClass;
	}

	public void setExtensionClass(Class extensionClass) {
		m_extensionClass = extensionClass;
	}

	public PKExtensionPoint() {
    }

    @Override
    protected Class getDescriptionClass() {
    	return this.getClass();
    }
    
    IPKRootExtensionPoint m_erp;
    
    private boolean m_singleExtensionInstance = true;

    Map<String, IPKExtension> m_extensions =  new LinkedHashMap<String, IPKExtension>();

    public void setSingleExtensionInstances(boolean value){
        m_singleExtensionInstance = value;
    }
    public boolean isSingleExtensionInstances() {
        return m_singleExtensionInstance;
    }
   
    public IPKExtension getExtension(String extensionId)
            throws EExtensionNotFound {
        IPKExtension e = m_extensions.get(extensionId);
        if(null == e){
            throw new EExtensionNotFound(extensionId, this);
        }
        return e;
    }

    public Map<String, IPKExtension> getExtensions() {
        return m_extensions;
    }
    
    
    
    public IPKExtension putExtension(String extensionId,
            Object obj) throws EExtensionPointNotFound {
        
        ObjectInstantiator oi = null;
        if(obj instanceof Class){
            oi = new ObjectInstantiator(new ObjectFactoryFromClassName(((Class)obj).getName()));    
        } else{
            oi = new ObjectInstantiator(new ObjectFactoryFromObject(obj));
        }
        if( (null != getExtensionClass()) && (! getExtensionClass().isAssignableFrom(oi.getObjectClass())) ){
        	throw new EIncompatibleExtensionClass(this.getId(), this.getExtensionClass(), extensionId, oi.getObjectClass());
        }
        IPKExtension res = new PKExtension(this, oi);
        res.setId(extensionId);
        m_extensions.put(extensionId, res);
        return res;
    }
    
    public void removeExtension(String extensionId) {
        m_extensions.remove(extensionId);
    }
    
    public IPKExtension putExtension(Object obj)
            throws EExtensionPointNotFound {
        return putExtension(obj.getClass().getName(), obj);        
    }
    
    public IPKExtension putExtension(Class cls) throws EExtensionPointNotFound {
        return putExtension(cls.getName(), cls);
    }
    
   
	public IPKExtension getExtension(Class extensionClass)
			throws EExtensionNotFound {
		return getExtension(extensionClass.getName());
	}

	IPKRootExtensionPoint m_rep;

    public IPKRootExtensionPoint getRootExtensionPoint() {
        return m_rep;
    }

    public void setRootExtensionPoint(IPKRootExtensionPoint rep) {
        m_rep = rep;
    }
    
}
