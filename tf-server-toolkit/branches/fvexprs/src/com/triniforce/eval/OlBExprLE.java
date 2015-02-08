/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprLE extends OlBExprEquals {

    OlBExprLE(Object testValue) {
        super(testValue);
    }

    @Override
    boolean internal_evaluateValue(Object value) {
        Comparable cmp = (Comparable) value;
        return cmp.compareTo(m_workTestValue) <= 0;
    }
}