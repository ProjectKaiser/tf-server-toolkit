/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.soap;

public class NamedVar {

    private String m_name;
    private Object m_value;

    public NamedVar(String name, Object value) {
        m_name = name;
        m_value = value;
    }
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NamedVar)) return false;
        NamedVar other = (NamedVar) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.m_name==null && other.getName()==null) || 
             (this.m_name!=null &&
              this.m_name.equals(other.getName()))) &&
            ((this.m_value==null && other.getValue()==null) || 
             (this.m_value!=null &&
              this.m_value.equals(other.getValue())));
        __equalsCalc = null;
        return _equals;
    }
    
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    
    public Object getValue() {
        return m_value;
    }
    public void setValue(Object value) {
        m_value = value;
    }
    
    @Override
    public String toString() {
        return m_name+':'+m_value;
    }
    
    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getValue() != null) {
            _hashCode += getValue().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

}
