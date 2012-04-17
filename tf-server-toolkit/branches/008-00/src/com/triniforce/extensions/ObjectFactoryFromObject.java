/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import com.triniforce.utils.TFUtils;

public class ObjectFactoryFromObject implements ISimpleObjectFactory{

    private final Object m_obj;
    
    public ObjectFactoryFromObject(Object obj) {
        m_obj = obj;
    }
    public Object getInstance() {
        return m_obj;
    }
    public Class getObjectClass() {
        TFUtils.assertNotNull(m_obj, "m_obj is not assigned");
        return m_obj.getClass();
    }

}
