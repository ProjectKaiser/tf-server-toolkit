/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import java.util.Map;

public interface IPKRootExtensionPoint extends IPKExtensionBase {
    IPKExtensionPoint getEpPlugins();
    IPKExtensionPoint getEpFunctions();
    void putExtensionPoint(String extensionPointId, IPKExtensionPoint ep);
    void putExtensionPoint(IPKExtensionPoint ep);
    IPKExtensionPoint getExtensionPoint(String extensionPointId) throws EExtensionPointNotFound;
    IPKExtensionPoint getExtensionPoint(Class extensionPointClass) throws EExtensionPointNotFound;
    
    public IPKExtension getExtension(String extensionPointId, String extensionId) throws EExtensionPointNotFound, EExtensionNotFound;
    public IPKExtension getExtension(Class extensionPointClass, Class extensionClass) throws EExtensionPointNotFound, EExtensionNotFound;
    
    /**
     * 
     * @return map where iteration order is same as insertion order
     * 
     */    
    Map<String, IPKExtensionPoint> getExtensionPoints();    

}
