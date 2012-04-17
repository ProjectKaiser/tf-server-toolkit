/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.select;


public class EWrongParam extends RuntimeException {
    private static final long serialVersionUID = 5741319787365781337L;
    
    private String m_paramName;

    public EWrongParam(String paramName) {
        super(paramName);
        m_paramName = paramName;
    }
    
    public String getParamName(){
        return m_paramName;        
    }
}
