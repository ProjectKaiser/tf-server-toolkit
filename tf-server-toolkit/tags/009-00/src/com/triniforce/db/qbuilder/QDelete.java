/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import com.triniforce.db.qbuilder.Err.EPrefixNotFound;
import com.triniforce.db.qbuilder.QStatement.QStatementWithWhere;

public class QDelete extends QStatementWithWhere  implements IQContext{
    protected IQTable m_qt;
    
    public QDelete(IQTable qt) {
        m_qt = qt;
    }
    
    @Override
    public QDelete where(WhereClause wc){
        super.where(wc);
        return this;
    }
    
    @Override
    public IQTable getTable(String prefix) throws EPrefixNotFound {
        if( ! m_qt.getPrefix().equals(prefix)){
            throw new Err.EPrefixNotFound(prefix);
        }
        return m_qt;
    }
    
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer("delete from "); //$NON-NLS-1$
        res.append(m_qt.getDbName());
        if(m_qt.getPrefix().length()>0){
            res.append(" ").append(m_qt.getPrefix()); //$NON-NLS-1$
        }
        appendClause(res, m_where);        
        return res.toString();
    }    

}
