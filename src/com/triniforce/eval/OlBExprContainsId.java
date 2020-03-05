/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eval;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds space-seperated id
 */
public class OlBExprContainsId  extends OlBExprColumnVsValue{
	@Override
    public String getOpName() {
        return "CONTAINS_ID";
    }

	@Override
	Object calcComparableTestValue(Object testValue, Object columnValue) {
        if(null == testValue){
            return null;
        }
        testValue = testValue.toString().toLowerCase(Locale.ENGLISH);
        return Pattern.compile( "(^|\\s)" + Pattern.quote(testValue.toString()) +"($|\\s)", Pattern.CASE_INSENSITIVE);
	}
	
	@Override
	boolean compareNotNullValues(Object columnValue, Object testValue) {
        String sv = columnValue.toString().toLowerCase(Locale.ENGLISH);
        Matcher m = ((Pattern)testValue).matcher(sv);
        return m.find();
	}
	
	public OlBExprContainsId() {
	}
	
    public OlBExprContainsId(Object value) {
        super(value);
    }
	

}
