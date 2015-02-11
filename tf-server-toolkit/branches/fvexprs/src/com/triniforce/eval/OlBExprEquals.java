/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprEquals extends OlBExprColumnVsValue {

    
    @Override
    Boolean bothNulls() {
        return true;
    }
    
    public OlBExprEquals() {
    }
    
    
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