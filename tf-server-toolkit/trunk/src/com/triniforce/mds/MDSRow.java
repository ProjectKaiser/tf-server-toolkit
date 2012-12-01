/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MDSRow implements IMDSRow{

    List m_values;
    
    public MDSRow() {
        m_values = new ArrayList();
    }
    public MDSRow(int size){
        m_values = new ArrayList(size);
    }
    
    public Object get(int idx) throws IndexOutOfBoundsException {
        return m_values.get(idx);
    }

    public void set(int idx, Object value) throws IndexOutOfBoundsException {
        m_values.set(idx, value);
    }

    public void appendArray(Object[] values, int fromIndex, int cnt) {
        for(int i = 0;i< cnt; i++){
            add(values[i + fromIndex]);
        }
    }

    public void appendList(List values, int fromIndex, int cnt) {
        for(int i = 0;i< cnt; i++){
            add(values.get(i + fromIndex));
        }        
    }

    public int size() {
        return m_values.size();
    }

    public void add(Object value) {
        m_values.add(value);
    }

    public void appendArray(Object[] values) {
        appendArray(values, 0, values.length);
        
    }

    public void appendList(List values) {
        appendList(values, 0, values.size());
    }

    public Iterator<Object> iterator() {
        return m_values.iterator();
    }
    
    @Override
    public String toString() {
        return m_values.toString();
    }

}
