/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.IName;

/**
 * This class is used by CollectionViewRequest
 */
@PropertiesSequence( sequence = {"functionName", "fieldName", "resultName"})
public class FieldFunctionRequest {
    protected String m_fieldName;
    protected String m_resultName;
    protected String m_functionName;
    
    public FieldFunctionRequest() {
    }
    public FieldFunctionRequest(String fieldName, String functionName, String resultName) {
        m_fieldName = fieldName;
        m_functionName = functionName;
        m_resultName = resultName;
    }
    public FieldFunctionRequest(IName field, Class function, String resultName) {
    	this(field.getName(), function.getName(), resultName);
    }
    
    
    public FieldFunctionRequest(String fieldName, Class cls) {
        this(fieldName, cls.getName(), cls.getSimpleName());
    }
    
    public String getFieldName() {
        return m_fieldName;
    }
    public void setFieldName(String fieldName) {
        m_fieldName = fieldName;
    }
    public String getFunctionName() {
        return m_functionName;
    }
    public void setFunctionName(String functionName) {
        m_functionName = functionName;
    }
    public String getResultName() {
        return m_resultName;
    }
    public void setResultName(String resultName) {
        m_resultName = resultName;
    }
}
