/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.triniforce.db.dml.IResSet;

public class MDS implements Iterable<IMDSRow>{
    private List<IMDSRow> m_rows = new ArrayList<IMDSRow>();
    private Map<String, Integer> m_namesMap = new HashMap<String, Integer>();

    public List<IMDSRow> getRows() {
        return m_rows;
    }

    public Map<String, Integer> getNamesMap() {
        return m_namesMap;
    }

    public void setNamesMap(Map<String, Integer> namesMap) {
        m_namesMap = namesMap;
    }
    
    public IMDSRow addArray(Object values[]){
        return addArray(values, 0, values.length);
    }
    
    public IMDSRow addArray(Object values[], int fromIndex, int cnt){
        IMDSRow r = new MDSRow(cnt);
        r.appendArray(values, fromIndex, cnt);
        m_rows.add(r);
        return r;
    }
    public IMDSRow addList(List values){
        return addList(values, 0, values.size());
    }
    public IMDSRow addList(List values, int fromIndex, int cnt){
        IMDSRow r = new MDSRow(cnt);
        r.appendList(values, fromIndex, cnt);
        m_rows.add(r);
        return r;
    }
    
    public IResSet calcIResSet(){
        return null;
    }

    public Iterator<IMDSRow> iterator() {
        return m_rows.iterator();
    }
    
}
