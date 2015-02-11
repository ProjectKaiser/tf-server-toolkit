/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprLE extends OlBExprColumnVsValue {

    public OlBExprLE() {
    }
    
    public OlBExprLE(Object testValue) {
        super(testValue);
    }

    @Override
    boolean compareNotNullValues(Object columnValue, Object testValue) {
        Comparable cmp = (Comparable) columnValue;
        return cmp.compareTo(testValue) <= 0;
    }

    @Override
    public String getOpName() {
        return "<=";
    }
    
}