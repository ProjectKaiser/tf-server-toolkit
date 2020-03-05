/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.triniforce.utils.IName;

public abstract class BasicResSet implements IResSet {
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
    
    Map<String, Integer> m_idx ;
    
    public abstract List<String> getColumns();
    public abstract boolean first();
    public abstract boolean next();
    
    public boolean isRowBeg(){
    	return true;
    }
    
    public int getIndexOf(String colName) throws EColumnNotFound{
        if(m_idx == null){
            m_idx = new HashMap<String, Integer>();
            for (int i = 0; i < getColumns().size(); i++) {
                m_idx.put(getColumns().get(i).toLowerCase(Locale.ENGLISH), i+1);//1 - based               
            }
        }
        Object res = m_idx.get(colName);
        if(null == res){
            throw new EColumnNotFound(colName);
        }
        return (Integer) res;
    }
    
    public Object getObject(String colName){
        return getObject(getIndexOf(colName));
    }
    public Object getObject(IName colName){
        return getObject(getIndexOf(colName.getName()));
    }
    
    public Object getSoapObject(String colName){
        return getSoapObject(getIndexOf(colName));
    }
    public Object getSoapObject(IName colName){
        return getSoapObject(getIndexOf(colName.getName()));
    }
    
    
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
        int cnt = 0;
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
            cnt ++;
        }
        
        res.append("Record count:" + cnt);
        
        try{
            first();
        }catch(Exception e){
            res.append("\n!!! Note, this dataset MAY NOT BE USED anymore:" + e);
        }
        
        return res.toString();
    }
}
