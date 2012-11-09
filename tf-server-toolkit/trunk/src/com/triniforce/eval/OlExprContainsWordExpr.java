/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OlExprContainsWordExpr extends Ol_ExprString{
    
    private Pattern m_pattern;

    public OlExprContainsWordExpr(Object value) {
        super(value);
        StringBuffer sb = new StringBuffer("(^|[\\s.,;\\-]+)");
        for(int i=0;i<m_value.length();i++){
            sb.append("\\" + m_value.charAt(i));
        }
        m_pattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }
    
    @Override
    public boolean evaluateString(String value) {
        Matcher m = m_pattern.matcher(value);
        return m.find();
    }
    
}
