/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.TFUtils;

@Deprecated
public class ObjectFactoryFromClassName implements ISimpleObjectFactory {

    private final String m_className;
    private Class m_class;

    public ObjectFactoryFromClassName(String className) {
        m_className = className;
    }

    public Object getInstance() {
        try {
            return getObjectClass().newInstance();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

    public Class getObjectClass() {
        TFUtils.assertNotNull(m_className, "m_obj is not assigned");
        try {
            if (null == m_class) {
                m_class = Class.forName(m_className);
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return m_class;
    }
    
}
