/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

public interface IResSet {
    boolean next();    
  
    
    /**
     * @param columnIndex the first column is 1, the second is 2, ... 
     * @return
     * @throws IndexOutOfBoundsException if column index is wrong
     */
    public Object getObject(int columnIndex) throws IndexOutOfBoundsException;   
}