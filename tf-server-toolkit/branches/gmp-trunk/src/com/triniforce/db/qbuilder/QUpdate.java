/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import java.util.Set;

import com.triniforce.db.qbuilder.Err.EPrefixNotFound;
import com.triniforce.db.qbuilder.QStatement.QStatementWithWhere;

public class QUpdate extends QStatementWithWhere  implements IQContext{
    protected IQTable m_qt;
    
    public QUpdate(IQTable qt) {
        m_qt = qt;
    }
    
    @Override
    public QUpdate where(WhereClause wc){
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
        StringBuffer res = new StringBuffer("update "); //$NON-NLS-1$
        res.append(m_qt.getDbName());
        if(m_qt.getPrefix().length()>0){
            res.append(" ").append(m_qt.getPrefix()); //$NON-NLS-1$
        }
        res.append(" set"); //$NON-NLS-1$
        
        Set<String> colNames = m_qt.getCols().keySet();
        boolean bComma = false;
        for (String col : colNames) {
            if (bComma) {
                res = res.append(", "); //$NON-NLS-1$
            } else {
                res = res.append(" "); //$NON-NLS-1$
                bComma = true;
            }
            res.append(m_qt.getPrefixedCol(col)).append(" = ?");  //$NON-NLS-1$
        }        
        appendClause(res, m_where);        
        return res.toString();
    }

}
