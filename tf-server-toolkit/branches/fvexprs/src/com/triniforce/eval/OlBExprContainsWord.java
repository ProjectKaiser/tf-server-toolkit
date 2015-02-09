/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OlBExprContainsWord extends Ol_ExprString{
    
    private Pattern m_pattern;

    @Override
    public String getOpName() {
        return "CONTAINS_WORD";
    }
    
    public OlBExprContainsWord(Object value) {
        super(value);
        m_value = m_value.toLowerCase();
        m_pattern = Pattern.compile( "(^|[\\s@.,;\\-]+)" + Pattern.quote(m_value.toString()), Pattern.CASE_INSENSITIVE);
    }
    
    @Override
    public boolean evaluateString(String value) {
        if(null == value) return false;
        String sv = ((String)value).toLowerCase();
        Matcher m = m_pattern.matcher(sv);
        return m.find();
    }
    
}
