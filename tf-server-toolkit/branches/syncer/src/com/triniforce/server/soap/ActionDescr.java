/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.soap;

import java.io.Serializable;

public class ActionDescr implements Serializable{
    private static final long serialVersionUID = 1L;

    protected String m_name;

    protected String m_data;
    public String getData() {
        return m_data;
    }

    public ActionDescr() {
     }

    public ActionDescr(String name, String data) {
        m_name = name;
        m_data = data;
    }

    public void setData(String data) {
        m_data = data;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((m_data == null) ? 0 : m_data.hashCode());
        result = PRIME * result + ((m_name == null) ? 0 : m_name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ActionDescr other = (ActionDescr) obj;
        if (m_data == null) {
            if (other.m_data != null)
                return false;
        } else if (!m_data.equals(other.m_data))
            return false;
        if (m_name == null) {
            if (other.m_name != null)
                return false;
        } else if (!m_name.equals(other.m_name))
            return false;
        return true;
    }
}
