/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlExprLE extends OlExprEQ {

    OlExprLE(Object testValue) {
        super(testValue);
    }

    @Override
    boolean evaluateValue(Object value) {
        Comparable cmp = (Comparable) value;
        return cmp.compareTo(m_workTestValue) <= 0;
    }
}