/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

public class PKExtensionClass implements IPKExtensionClass{

    private IPKExtension m_ext;

    public IPKExtensionPoint getExtensionPoint() {
        return m_ext.getExtensionPoint();
    }
    public void setExtension(IPKExtension ext) {
        m_ext = ext;
    }

    public IPKExtension getExtension() {
        return m_ext;
    }
}
