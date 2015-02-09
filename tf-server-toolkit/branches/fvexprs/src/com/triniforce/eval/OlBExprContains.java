/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprContains extends Ol_ExprString {
    public OlBExprContains(Object value) {
		super(value);
		m_value = m_value.toLowerCase();
	}

	@Override
    public boolean evaluateString(String value) {
        if(null == value) return false;
        String sv = ((String)value).toLowerCase();
        return sv.indexOf(m_value) >=0;
    }
	
    @Override
    public String getOpName() {
        return "CONTAINS";
    }
    
}