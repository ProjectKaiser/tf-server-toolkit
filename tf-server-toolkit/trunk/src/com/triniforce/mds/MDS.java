/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.triniforce.db.dml.BasicResSet;
import com.triniforce.db.dml.IResSet;
import com.triniforce.utils.IName;

public class MDS implements Iterable<IMDSRow>{
    private List<IMDSRow> m_rows = new ArrayList<IMDSRow>();
    private Map<String, Integer> m_namesMap = new HashMap<String, Integer>();

    public List<IMDSRow> getRows() {
        return m_rows;
    }

    public Map<String, Integer> getNamesMap() {
        return m_namesMap;
    }

    
    /**
     * This operation does NOT append values to rows
     */
    public void appendNames(List<String> names){
        int idx = -1;
        for( Entry<String, Integer> e: m_namesMap.entrySet()){
            if(e.getValue() > idx){
                idx = e.getValue();
            }
        }
        for(String name: names){
            m_namesMap.put(name, ++idx);
        }
    }
    
    public void appendNames(String... names){
        appendNames(Arrays.asList(names));        
    }
    
    public void appendINames(IName... names){
        List<String> sNames = new ArrayList<String>(names.length);
        for(IName n: names){
            sNames.add(n.getName());
        }
        appendNames(sNames);
        
    }
    
    public void setNamesMap(Map<String, Integer> namesMap) {
        m_namesMap = namesMap;
    }
    
    public IMDSRow appendRowAsArray(Object values[]){
        return appendRowAsArray(values, 0, values.length);
    }
    
    public void setCell(IMDSRow row, String name, Object value){
        int idx = getIndexOf(name);
        row.set(idx, value);
    }
    
    public void setCell(IMDSRow row, IName name, Object value){
        setCell(row, name.getName(), value);
    }
    
    public IMDSRow appendRowAsArray(Object values[], int fromIndex, int cnt){
        IMDSRow r = new MDSRow(cnt);
        r.appendArray(values, fromIndex, cnt);
        m_rows.add(r);
        return r;
    }
    public IMDSRow appendRowAsList(List values){
        return appendRowAsList(values, 0, values.size());
    }
    public IMDSRow appendRowAsList(List values, int fromIndex, int cnt){
        IMDSRow r = new MDSRow(cnt);
        r.appendList(values, fromIndex, cnt);
        m_rows.add(r);
        return r;
    }
    
    class MyResSet extends BasicResSet implements IResSet{
        int idx = -1;
        public boolean next() {
            idx++;
            return idx < m_rows.size();
        }
        
        public Object getObject(int columnIndex) throws IndexOutOfBoundsException {
            return getRows().get(idx).get(columnIndex);
        }

        public List<String> getColumns() {
            return null;
        }
        
    }
    
    public IResSet getIResSet(){
        return this.new MyResSet();
    }

    public Iterator<IMDSRow> iterator() {
        return m_rows.iterator();
    }
    
    @SuppressWarnings("serial")
	public static class ColumnNotFound extends RuntimeException{
		public ColumnNotFound(String s) {
			super("Column name: " + s);
		}
	}

    public int getIndexOf(String colName){
        Integer index = m_namesMap.get(colName);
        if (index == null) {
            throw new ColumnNotFound(colName);
        } else {
            return index;
        }
    }
    
    public Object getCell(IMDSRow row, String col) throws IndexOutOfBoundsException {
    	if (row == null) throw new NullPointerException("row");
    	if (col == null) throw new NullPointerException("col");
  		return row.get(getIndexOf(col));
    }
    
//    public void setCell(IMDSRow row, String col) throws IndexOutOfBoundsException {
//
//        if (row == null) throw new NullPointerException("row");
//        if (col == null) throw new NullPointerException("col");
//        
//        Integer index = m_namesMap.get(col);
//        if (index == null) {
//            throw new ColumnNotFound(col);
//        } else {
//            return row.get(index-1);
//        }
//    }
    
    public Object getCell(IMDSRow row, IName col) throws IndexOutOfBoundsException {
    	if (col == null) throw new NullPointerException("col");
    	return getCell(row, col.getName());
    }
    
    public IMDSRow appendNullRow(){
        final int sz = getNamesMap().size();
        MDSRow row = new MDSRow(sz);
        Object vals[] = new Object[sz];
        row.appendArray(vals);
        appendRow(row);
        return row;
    }
    
    public IMDSRow appendRow(IMDSRow src){
    	if (src == null) {
        	throw new NullPointerException("src");
        }
       	m_rows.add(src);
       	return m_rows.get(m_rows.size()-1);
    }
    
    @Override
    public String toString() {
    	
    	StringBuffer res = new StringBuffer("\n");
    	
    	String[] keys = new String[m_namesMap.size()];
    	m_namesMap.keySet().toArray(keys);
    	
    	for (int i = 0; i < keys.length; i++) {
			if (i > 0) res.append("\t");
			res.append(keys[i]);
		}
		res.append("\n");
				
		for (IMDSRow row : m_rows) {
			for (int i = 0; i < keys.length; i++) {
				int index = m_namesMap.get(keys[i]);
				if (i > 0) res.append("\t");
				if (row.get(index) == null) {
					res.append("null");
				} else {
					res.append(row.get(index).toString());
				}
				
			}
			res.append("\n");
		}
		return res.toString();
    }
    
}
