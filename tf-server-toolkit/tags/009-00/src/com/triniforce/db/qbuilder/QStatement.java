/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import com.triniforce.db.qbuilder.Err.EPrefixNotFound;

public class QStatement  implements IQContext{
  
    
    public static class QStatementWithWhere extends QStatement{
        protected WhereClause m_where;
        
        public QStatement where(WhereClause wc){
            m_where = wc;
            wc.bindToContext(this);
            return this;
        }        
    }
    
    
    protected void appendClause(StringBuffer res, Object obj){
        if(null == obj) return;
        String s = obj.toString();
        if(s.length()==0)return;
        res.append(' ').append(s);        
    }

    public IQTable getTable(String prefix) throws EPrefixNotFound {
        throw new RuntimeException("Not implemented");//$NON-NLS-1$
    }    
    
}
