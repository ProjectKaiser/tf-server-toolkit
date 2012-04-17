/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import com.triniforce.utils.TFUtils;

public class PKExtensionBase implements IPKExtensionBase {
    private String m_wikiDescription = null;
    private String m_supplier = "";
    private String m_pluginId = "";
    private String m_id;
    
    protected Class getDescriptionClass(){
        return null;
    }
    public PKExtensionBase() {
    }
    
    public void setWikiDescription(String wikiDescription) {
        m_wikiDescription = wikiDescription;
    }

    public void setProvider(String supplier) {
        m_supplier = supplier;
    }

    public String getWikiDescription() {
        if( null == m_wikiDescription){
            m_wikiDescription = "";
            try{
                Class eoClass = getDescriptionClass();
                if(null != eoClass){
                    m_wikiDescription = TFUtils.readResource(eoClass, eoClass.getSimpleName() + ".wiki");
                }                    
            }catch(Throwable t){
            }
        }
        return m_wikiDescription;
    }

    public String getProvider(){
        return m_supplier;
    }
    
	public void setPluginId(String pluginId) {
	    m_pluginId = pluginId;
	}
    
    public String getPluginId() {
        return m_pluginId;
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        m_id = id;
    }
}
