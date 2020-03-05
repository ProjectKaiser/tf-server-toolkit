/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.Locale;

public class OlBExprContains extends OlBExprColumnVsValue {
    
    public OlBExprContains() {
    }
    
    public OlBExprContains(Object value) {
		super(value);
	}
    
    @Override
    Object calcComparableTestValue(Object testValue, Object columnValue) {
        if(null == testValue){
            return null;
        }
        return testValue.toString().toLowerCase(Locale.ENGLISH);
        
    }

    @Override
    public String getOpName() {
        return "CONTAINS";
    }

    @Override
    boolean compareNotNullValues(Object columnValue, Object testValue) {
        String sv = columnValue.toString().toLowerCase(Locale.ENGLISH);
        return sv.indexOf((String)testValue) >=0;
    }
    
}