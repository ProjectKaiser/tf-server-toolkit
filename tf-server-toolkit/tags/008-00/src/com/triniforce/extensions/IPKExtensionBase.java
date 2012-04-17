/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

public interface IPKExtensionBase {
    String getWikiDescription();
    String getPluginId();
    String getId();
    void setPluginId(String value);
    void setWikiDescription(String wikiDescription);
    void setId(String id);
}    
