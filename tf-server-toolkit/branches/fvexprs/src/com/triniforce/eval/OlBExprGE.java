/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprGE extends OlBExprEquals {

    OlBExprGE(Object testValue) {
        super(testValue);
    }

    @Override
    boolean internal_evaluateValue(Object value) {
        Comparable cmp = (Comparable) value;
        return cmp.compareTo(m_workTestValue) >= 0;
    }
    
    @Override
    String internal_getOpName() {
    	return ">";
    }
}