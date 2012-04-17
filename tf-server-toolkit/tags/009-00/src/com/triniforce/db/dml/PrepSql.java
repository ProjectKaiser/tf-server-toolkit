/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;

import com.triniforce.db.qbuilder.QStatement;

public class PrepSql {
    
    protected String m_str = null;
    
    public QStatement buildSql(){
        throw new RuntimeException("Not supported");//$NON-NLS-1$
    }
    public String toString(){
        if( null == m_str){
            m_str = buildSql().toString();
        }
        return m_str;
    }
}
