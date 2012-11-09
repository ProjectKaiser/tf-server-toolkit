/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlExprContains extends Ol_ExprString {
    public OlExprContains(Object value) {
		super(value);
		m_value = m_value.toLowerCase();
	}

	@Override
    public boolean evaluateString(String value) {
        if(null == value) return false;
        String sv = ((String)value).toLowerCase();
        return sv.indexOf(m_value) >=0;
    }
    
}