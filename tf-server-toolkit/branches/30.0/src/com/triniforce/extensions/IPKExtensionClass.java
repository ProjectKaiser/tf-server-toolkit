/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

public interface IPKExtensionClass {
    public void setExtension(IPKExtension ep);
    IPKExtension getExtension();
    IPKExtensionPoint getExtensionPoint();    
}
