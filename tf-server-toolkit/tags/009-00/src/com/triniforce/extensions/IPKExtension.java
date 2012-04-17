/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

public interface IPKExtension extends IPKExtensionBase{
    IPKExtensionPoint getExtensionPoint();
    void setExtensionPoint(IPKExtensionPoint ep);
    <T> T getInstance();
    Class getObjectClass();
}
