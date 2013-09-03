/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.soap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActivityData implements Serializable{
    private static final long serialVersionUID = 1L;
    protected List<ActionDescr> m_actionDescrs = new ArrayList<ActionDescr>();
	public List<ActionDescr> getActionDescrs() {
		return m_actionDescrs;
	}

	public void setActionDescrs(List<ActionDescr> actionDescrs) {
		m_actionDescrs = actionDescrs;
	}

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((m_actionDescrs == null) ? 0 : m_actionDescrs.hashCode());
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
        final ActivityData other = (ActivityData) obj;
        if (m_actionDescrs == null) {
            if (other.m_actionDescrs != null)
                return false;
        } else if (!m_actionDescrs.equals(other.m_actionDescrs))
            return false;
        return true;
    }
    

}
