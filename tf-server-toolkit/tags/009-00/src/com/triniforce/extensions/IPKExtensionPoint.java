/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import java.util.Map;

public interface IPKExtensionPoint extends IPKExtensionBase{
    
    boolean isSingleExtensionInstances();
    void setSingleExtensionInstances(boolean value); 
    
    /**
     * This version is needed to add extension from script
     */
    IPKExtension putExtension(String extensionId, Object obj) throws EExtensionPointNotFound;
    IPKExtension putExtension(Object obj) throws EExtensionPointNotFound;
    IPKExtension putExtension(Class cls) throws EExtensionPointNotFound;
    
    void removeExtension(String extensionId);
   
    /**
     * 
     * @return map where iteration order is same as insertion order
     * 
     */
    Map<String, IPKExtension> getExtensions();

   
    IPKExtension getExtension(String extensionId) throws EExtensionNotFound;
    IPKExtension getExtension(Class extensionClass) throws EExtensionNotFound;
    
	//query parent extension point (getExtensionPoint())
	
    public IPKRootExtensionPoint getRootExtensionPoint();
    void setRootExtensionPoint(IPKRootExtensionPoint rep); 
    
    Class getExtensionClass();
    void setExtensionClass(Class extensionClass);
    
}
