/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

import java.util.List;

import com.triniforce.utils.IName;

public interface IResSet {
    boolean next();  
    boolean first();
    
    boolean isRowBeg();
  
    
    public int getIndexOf(String name) throws EColumnNotFound;
    
    /**
     * @param columnIndex the first column is 1, the second is 2, ... 
     * @return
     * @throws IndexOutOfBoundsException if column index is wrong
     */
    public Object getObject(int columnIndex) throws IndexOutOfBoundsException;
    public Object getObject(IName colName) throws EColumnNotFound;
    public Object getObject(String colName) throws EColumnNotFound;
    
    /**
     * 
     * Converts values to be used for SOAP deserialization. E.g. Timestamp is converted to Calendar and stuff like this
     *  
     */
    public Object getSoapObject(int columnIndex) throws IndexOutOfBoundsException;
    public Object getSoapObject(IName colName) throws EColumnNotFound;
    public Object getSoapObject(String colName) throws EColumnNotFound;
    
    List<String> getColumns();
}
