/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.utils.TFUtils;

public abstract class Ol_ExprString extends OlBExpr{
    String m_value;
    public Ol_ExprString(Object value) {
        TFUtils.assertNotNull(value, "Argument for " + this.getClass().getName()+ " must have not null String value");
        m_value = value.toString();
    }
    
    @Override
    public boolean evaluate(Object value, IOlColumnGetter vg) {
        return evaluateString( null!= value? value.toString(): null);
    }
    
    public boolean evaluateString(String value){
        return false;
    }
    
    @Override
    public String toString() {
    	return super.toString() +"(" + m_value + ")";
    }
    

}
