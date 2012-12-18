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
import java.util.Map.Entry;

import com.triniforce.db.dml.IResSet;
import com.triniforce.db.dml.ResSet;
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
        int idx = 0;
        for( Entry<String, Integer> e: m_namesMap.entrySet()){
            if(e.getValue() > idx){
                idx = e.getValue();
            }
        }
        for(String name: names){
            m_namesMap.put(name, ++idx);
        }
    }
    
    public void setNamesMap(Map<String, Integer> namesMap) {
        m_namesMap = namesMap;
    }
    
    public IMDSRow appendRowAsArray(Object values[]){
        return appendRowAsArray(values, 0, values.length);
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
    
    public IResSet getIResSet(){
        return new IResSet() {
            int idx = -1;
            public boolean next() {
                idx++;
                return idx < m_rows.size();
            }
            
            public Object getObject(int columnIndex) throws IndexOutOfBoundsException {
                return getRows().get(idx).get(columnIndex);
            }
        };
    }

    public Iterator<IMDSRow> iterator() {
        return m_rows.iterator();
    }
    
    public Object getCell(IMDSRow row, int col) throws IndexOutOfBoundsException{
        //TODO
        return null;
    }
    public Object getCell(IMDSRow row, String col) throws IndexOutOfBoundsException{
        //TODO
        return null;
    }
    public Object getCell(IMDSRow row, IName col) throws IndexOutOfBoundsException{
        //TODO
        return null;
    }
    
//    public static LongListResponse createFromResSet(ResSet resSet, List<IName> extraColumns){
//        
//        ResultSetMetaData md;
//        try {
//            md = resSet.getResultSet().getMetaData();
//            String colNames[] = new String[md.getColumnCount() + (null == extraColumns?0:extraColumns.size())];
//            for (int i = 0; i < colNames.length; i++) {
//                colNames[i] = md.getColumnName(i + 1).toLowerCase(); 
//                
//            }
//            LongListResponse res = new LongListResponse(colNames);
//            while(resSet.next()){
//                Object row[] = new Object[colNames.length];
//                for (int i = 0; i < md.getColumnCount(); i++) {
//                    row[i] = resSet.getObject(i + 1);                    
//                }
//                res.addRow(row);
//            }
//            return res;
//        } catch (Exception e) {
//            ApiAlgs.rethrowException(e);
//        }
//        
//        return null;
//        
//    }
    public static MDS createFromResSet(ResSet resSet){
        //TODO
        return null;
    }
    
    public IMDSRow appendRow(IMDSRow src){
        //TODO
        return null;        
    }
    
}
