/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.utils.TFUtils;

public abstract class Ol_ExprString extends OlExpr{
    String m_value;
    public Ol_ExprString(Object value) {
        TFUtils.assertTrue(value instanceof String, "Argument for " + this.getClass().getName()+ " must have not null String value");
        m_value = (String) value;
    }
    
    @Override
    public boolean evaluate(Object value) {
        return evaluateString((String) value);
    }
    
    public boolean evaluateString(String value){
        return false;
    }
    

}
