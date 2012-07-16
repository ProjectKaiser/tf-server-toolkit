/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;


public class PKExtension extends PKExtensionBase implements IPKExtension{

    private IPKExtensionPoint m_extensionPoint;
    private ObjectInstantiator m_oi;

    public PKExtension() {
    }
    
    @Override
    protected Class getDescriptionClass() {
        return m_oi.getObjectClass();
    }
    
    PKExtension(PKExtensionPoint ep, ISimpleObjectFactory of){
        m_extensionPoint = ep;
        m_oi = new ObjectInstantiator(of);
    }
    
	public IPKExtensionPoint getExtensionPoint() {
		return m_extensionPoint;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
    public <T> T getInstance() {
        m_oi.setSingle(m_extensionPoint.isSingleExtensionInstances());
	    if(m_oi.isNewInstance()){
	        T t =  (T) m_oi.getInstance();
	        if(t instanceof IPKExtensionClass){
	            ((IPKExtensionClass)t).setExtension(this);
	        }
	        return t;
	    } else {
	        return (T) m_oi.getInstance();
	    }
    }

    public void setExtensionPoint(IPKExtensionPoint ep) {
        m_extensionPoint = ep;
    }
	
}
