/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import java.util.Set;


public class QInsert extends QStatement {
    protected IQTable m_qt;
    
    public QInsert(IQTable qt) {
        m_qt = qt;        
    }
    
  
    @Override  
    public String toString() {
        StringBuffer res = new StringBuffer("insert into "); //$NON-NLS-1$
        res.append(m_qt.getDbName());
        if(m_qt.getPrefix().length()>0){
            res.append(" ").append(m_qt.getPrefix()); //$NON-NLS-1$
        }
        res.append(" ("); //$NON-NLS-1$
        Set<String> colNames = m_qt.getCols().keySet();
        boolean bComma = false;
        for (String col : colNames) {
            if (bComma) {
                res = res.append(", "); //$NON-NLS-1$
            } else {
                res = res.append(" "); //$NON-NLS-1$
                bComma = true;
            }
            res.append(m_qt.getPrefixedCol(col)); 
        }        
        res.append(" ) values ("); //$NON-NLS-1$
        bComma = false;
        for(int i =0; i< colNames.size(); i++){
            if (bComma) {
                res = res.append(", ?"); //$NON-NLS-1$
            } else {
                res = res.append(" ?"); //$NON-NLS-1$
                bComma = true;
            }
        }
        res.append(" )");        //$NON-NLS-1$
       
        return res.toString();
    }    

}
