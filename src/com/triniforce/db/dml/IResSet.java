/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

import java.util.List;

public interface IResSet {
    boolean next();    
  
    
    /**
     * @param columnIndex the first column is 1, the second is 2, ... 
     * @return
     * @throws IndexOutOfBoundsException if column index is wrong
     */
    public Object getObject(int columnIndex) throws IndexOutOfBoundsException;
    
    /**
     * 
     * Converts values to be used for SOAP deserialization. E.g. Timestamp is converted to Calendar and stuff like this
     *  
     */
    public Object getSoapObject(int columnIndex) throws IndexOutOfBoundsException;
    
    List<String> getColumns();
}
