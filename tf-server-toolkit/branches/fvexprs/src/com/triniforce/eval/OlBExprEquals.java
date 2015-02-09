/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprEquals extends OlBExprColumnVsValue {

    public OlBExprEquals(Object testExpr) {
        super(testExpr);
    }

    public String getOpName() {
		return "=";
	}

    @Override
    boolean compareNotNullValues(Object columnValue, Object testValue) {
        return columnValue.equals(testValue);
    }
    
}