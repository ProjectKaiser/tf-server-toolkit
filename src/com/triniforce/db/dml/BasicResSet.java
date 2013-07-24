/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

import java.sql.Timestamp;
import java.util.Calendar;

public abstract class BasicResSet {
    public abstract Object getObject(int columnIndex) throws IndexOutOfBoundsException;
    
    /**
     * 
     * Converts values to be used for SOAP deserialization. E.g. Timestamp is converted to Calendar and stuff like this
     *  
     */
    public Object getSoapObject(int columnIndex) throws IndexOutOfBoundsException{
        Object res = getObject(columnIndex);
        if(res instanceof Timestamp){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(((Timestamp)res).getTime());
            return cal; 
        }
        return res;        
    }
}
