/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.TFUtils;

public class ObjectFactoryFromClass implements ISimpleObjectFactory {

    private Class m_class;

    public ObjectFactoryFromClass(Class aClass) {
        m_class = aClass;
    }

    public Object getInstance(){
        try {
            return getObjectClass().newInstance();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

    public Class getObjectClass() {
        TFUtils.assertNotNull(m_class, "m_class is not assigned");
        return m_class;
    }
}
