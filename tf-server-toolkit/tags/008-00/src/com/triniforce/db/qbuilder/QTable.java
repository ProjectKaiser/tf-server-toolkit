/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;

public class QTable implements IQTable {

    Cols m_cols = new Cols();
    
    private final String m_dbName;
    private final String m_prefix;

    public QTable(String dbName, String prefix) {
        m_dbName = dbName;
        m_prefix = prefix;
    }
    public QTable(String name) {
        this(name, ""); //$NON-NLS-1$
    }    
    public QTable addCol(String colName) {
        if( m_cols.containsKey(colName) ){
            throw new Err.EColAlreadyExists(colName);
        }
        IQTable.Col col = new IQTable.Col();
        col.name = colName;
        m_cols.put(colName, col);
        return this;
    }


    public String getDbName() {
        return m_dbName;
    }

    public String getPrefix() {
        return m_prefix;
    }
    public Cols getCols() {
        return m_cols;
    }
    public String getPrefixedCol(String name) throws Err.EColNotFound {
        if( ! m_cols.containsKey(name)){
            throw new Err.EColNotFound(name);
        }
        return joinPrefixedCol(getPrefix(), name);
    }
    
    public static String joinPrefixedCol(String prefix, String col){
    	IDatabaseInfo dbInfo = ApiStack.getInterface(IDatabaseInfo.class);
    	String quoteString = dbInfo.getIdentifierQuoteString();
        if( prefix.length() > 0 ){
            return prefix+'.' + quoteString + col.toUpperCase() + quoteString;    
        }
        return quoteString + col.toUpperCase() + quoteString;        
    }
    public String calcPrefixedCol(String name) {
        return joinPrefixedCol(getPrefix(), name);
    }
    public IQTable addCol(IName icol) {
        return addCol(icol.getName());
    }
    public void onBuildWhere(WhereClause wc) {
    }

}
