/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import java.util.LinkedHashMap;

import com.triniforce.utils.IName;

public interface IQTable {
    
   
    public static class Col implements IName{
        String name;

        public String getName() {
            return name;
        }
    }

    @SuppressWarnings("serial") //$NON-NLS-1$
    public static class Cols extends LinkedHashMap<String, IQTable.Col> {
    };

    String getDbName();

    /**
     * @return prefix to use in select operation
     */
    String getPrefix();

    /**
     * @param colName
     *            for select statement
     * @return
     */
    IQTable addCol(String colName);
    
    IQTable addCol(IName icol);    

    /**
     * @param name
     * @return prefixed column name, of prefix is not "",
     * column name otherwise
     */
    String getPrefixedCol(String name) throws Err.EColNotFound;
    
    String calcPrefixedCol(String name);    
    
    Cols getCols();
    
    void onBuildWhere(WhereClause wc); 
}
