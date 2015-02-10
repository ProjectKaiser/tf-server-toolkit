/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

import java.util.LinkedHashSet;
import java.util.Set;

import com.triniforce.utils.TFUtils;

public abstract class VariantExpr{

    public Set<String> calcColumnNames() {
        return new LinkedHashSet<String>();
    }
    
    public static class ColumnValue extends VariantExpr{
        private String m_name;

        public ColumnValue(String name){
            setName(name);            
        }

        public String getName() {
            return m_name;
        }
        @Override
        public Set<String> calcColumnNames() {
            Set res = super.calcColumnNames();
            res.add(m_name);
            return res;
        }

        public void setName(String name) {
            TFUtils.assertNotNull(name, "Column name must not be empty");
            m_name = name;
        }
    }

}
