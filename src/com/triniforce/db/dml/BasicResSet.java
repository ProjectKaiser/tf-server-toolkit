/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

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
    
    public abstract List<String> getColumns();
    public abstract boolean first();
    public abstract boolean next();
    
    
    @Override
    public String toString() {
        
        StringBuffer res = new StringBuffer("\n");

        int ii = 1;
        List<String> cols = getColumns();
        for(String n:cols){
            if (ii > 1) res.append("\t");
            res.append(n);
            ii++;
        }
        res.append('\n');
        while(next()){
            for(int i = 1; i <= cols.size(); i++){
                if(i > 1) res.append('\t');
                if (getObject(i) == null) {
                    res.append("null");
                } else {
                    res.append(getObject(i).toString());
                }
            }
            res.append('\n');
        }
        try{
            first();
        }catch(Exception e){
            res.append("\n!!! Note, this dataset MAY NOT BE USED anymore:" + e);
        }
        
        return res.toString();
    }
}
