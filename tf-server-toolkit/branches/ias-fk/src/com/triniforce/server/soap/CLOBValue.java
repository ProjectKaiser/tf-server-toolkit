/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.soap;

import java.sql.Timestamp;
import java.text.MessageFormat;

public class CLOBValue {
    
    private String m_mimeType;
    private String m_value;
    private Long m_creatorId;
    private String m_creatorNickName;
    private Timestamp m_created;

    public CLOBValue(String mimeType, String value){
        this(mimeType, value, null, null, null);
    }
    
    public CLOBValue(String mimeType, String value, Timestamp created, String crNick, Long creator){
        m_mimeType = mimeType;
        m_value = value;
        m_created = created;
        m_creatorNickName = crNick;
        m_creatorId = creator;
    }

    public String getMimeType() {
        return m_mimeType;
    }

    public String getValue() {
        return m_value;
    }
    
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof CLOBValue)) return false;
        CLOBValue other = (CLOBValue) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.m_mimeType==null && other.getMimeType()==null) || 
             (this.m_mimeType!=null &&
              this.m_mimeType.equals(other.getMimeType()))) &&
            ((this.m_value==null && other.getValue()==null) || 
             (this.m_value!=null &&
              this.m_value.equals(other.getValue()))) &&
            ((this.m_created==null && other.getCreated()==null) || 
             (this.m_created!=null &&
              this.m_created.equals(other.getCreated()))) &&
            ((this.m_creatorNickName==null && other.getCreatorNickName()==null) || 
             (this.m_creatorNickName!=null &&
              this.m_creatorNickName.equals(other.getCreatorNickName()))) &&
            ((this.m_creatorId==null && other.getCreatorId()==null) || 
             (this.m_creatorId!=null &&
              this.m_creatorId.equals(other.getCreatorId())));
        
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getMimeType() != null) {
            _hashCode += getMimeType().hashCode();
        }
        if (getValue() != null) {
            _hashCode += getValue().hashCode();
        }
        if (getCreated() != null) {
            _hashCode += getCreated().hashCode();
        }
        if(getCreatorNickName() != null){
            _hashCode += getCreatorNickName().hashCode();
        }
        if(getCreatorId() != null){
            _hashCode += getCreatorId().hashCode();
        }
        
        __hashCodeCalc = false;
        return _hashCode;
    }

    @Override
    public String toString() {
        return MessageFormat.format("<{0}>{1}<{2}><{3}><{4}>", getMimeType(), getValue(), getCreated(), getCreatorNickName(), getCreatorId()); //$NON-NLS-1$
    }

    public Timestamp getCreated() {
        return m_created;
    }

    public String getCreatorNickName() {
        return m_creatorNickName;
    }

    public Long getCreatorId() {
        return m_creatorId;
    }

    public void setMimeType(String mimeType) {
        m_mimeType = mimeType;
    }

    public void setValue(String value) {
        m_value = value;
    }
    
    public void setCreated(Timestamp created) {
        m_created = created;
    }

    public void setCreatorNickName(String nickName) {
        m_creatorNickName = nickName;
    }

    public void setCreatorId(Long creatorId) {
        m_creatorId = creatorId;
    }
}
