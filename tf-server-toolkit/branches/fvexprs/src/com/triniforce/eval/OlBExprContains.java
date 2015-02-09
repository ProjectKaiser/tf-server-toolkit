/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprContains extends OlBExprColumnVsValue {
    public OlBExprContains(Object value) {
		super(value);
	}
    
    @Override
    Object calcComparableTestValue(Object testValue, Object columnValue) {
        if(null == testValue){
            return null;
        }
        return testValue.toString().toLowerCase();
        
    }

    @Override
    public String getOpName() {
        return "CONTAINS";
    }

    @Override
    boolean compareNotNullValues(Object columnValue, Object testValue) {
        String sv = columnValue.toString().toLowerCase();
        return sv.indexOf((String)testValue) >=0;
    }
    
}