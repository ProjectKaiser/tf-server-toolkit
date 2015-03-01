/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OlBExprContainsWord extends OlBExprColumnVsValue{
    

    public OlBExprContainsWord() {
    }
    
    @Override
    public String getOpName() {
        return "CONTAINS_WORD";
    }
    
    @Override
    Object calcComparableTestValue(Object testValue, Object columnValue) {
        if(null == testValue){
            return null;
        }
        testValue = testValue.toString().toLowerCase();
        return Pattern.compile( "(^|[\\s@.,;\\-]+)" + Pattern.quote(testValue.toString()), Pattern.CASE_INSENSITIVE);
    }
    
    public OlBExprContainsWord(Object value) {
        super(value);
    }

    @Override
    boolean compareNotNullValues(Object columnValue, Object testValue) {
        String sv = columnValue.toString().toLowerCase();
        Matcher m = ((Pattern)testValue).matcher(sv);
        return m.find();
    }
    
}
