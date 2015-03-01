/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

import java.util.LinkedHashSet;
import java.util.Set;

import com.triniforce.utils.TFUtils;

public abstract class ExprV{

    public Set<String> calcColumnNames() {
        return new LinkedHashSet<String>();
    }
    
    public static class ExprVColumn extends ExprV{
        private String m_name;

        public ExprVColumn() {
        }
        
        public ExprVColumn(String name){
            setColumnName(name);            
        }

        public String getColumnName() {
            return m_name;
        }
        @Override
        public Set<String> calcColumnNames() {
            Set res = super.calcColumnNames();
            res.add(m_name);
            return res;
        }

        public void setColumnName(String name) {
            TFUtils.assertNotNull(name, "Column name must not be empty");
            m_name = name;
        }
    }

}
