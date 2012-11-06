/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.utils.TFUtils;

public class OlExprContains extends OlExpr {
    String m_value;
    public OlExprContains(Object value){
        TFUtils.assertTrue(value instanceof String, "Argument for Contains expression  must have not null String value");
        m_value = ((String) value).toLowerCase();
    }
    @Override
    public boolean evaluate(Object value) {
        if(null == value) return false;
        if(! (value instanceof String)) return false;
        String sv = ((String)value).toLowerCase();
        return sv.indexOf(m_value) >=0;
    }
    
}