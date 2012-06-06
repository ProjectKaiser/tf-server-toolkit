/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;

/**
 *
 */
public interface IPrepSqlGetter {
    /**
     * 
     * Method is supposed to check if sql for given string
     * has been already built, built if necssary and finally 
     * return generated sql
     * 
     * @param cls is supposed to be a class inherited from SqlBuilder
     * @return String
     */
    String getSql(Class prepSql);   
    
}
