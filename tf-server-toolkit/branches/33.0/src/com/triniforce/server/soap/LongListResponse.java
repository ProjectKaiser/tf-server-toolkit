/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import java.sql.ResultSetMetaData;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.triniforce.db.dml.ResSet;
import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IName;
import com.triniforce.utils.TFUtils;


@PropertiesSequence( sequence = {"colNames", "values"})
public class LongListResponse extends BasicResponse{
	HashMap<String, Integer> m_colIndieces = new HashMap<String, Integer>();
    List<String> m_colNames = new ArrayList<String>();
    List<Object> m_values = new ArrayList<Object>();
    //private LongListRequest m_req;
    private int m_lineNo;
    private int m_startFrom;
    private int m_limit;
    
    /**
     * 0-ok, 1 -in process try later, 2 not exist 
     */
    private int m_sourceStatus = 0;
    
    
    @Override
    public String toString() {
        int rows = getRowsNumber();
        int cols = getColNames().length;
        StringBuffer res = new StringBuffer();

        res.append(Arrays.toString(getColNames()) + "\n");
        for(int i=0;i<rows;i++){
            Object vals[] = new Object[cols];
            for(int j =0; j<cols ; j++){
                vals[j] = getCell(i, j);
            }
            res.append(Arrays.toString(vals));
            res.append("\n");
        }
        return res.toString();
    }
    
    public int getSourceStatus() {
        return m_sourceStatus;
    }

    public void setSourceStatus(int sourceStatus) {
        m_sourceStatus = sourceStatus;
    }

    public LongListResponse(String ...colNames) {
        setColNames(colNames);
    }
    
    public LongListResponse(LongListRequest req) {
        if(req.getStartFrom() < 0)
            throw new IllegalArgumentException(LongListRequest.PROP_START_FROM);
        if(req.getLimit() < 0)
            throw new IllegalArgumentException(LongListRequest.PROP_LIMIT);
        
        m_startFrom = req.getStartFrom();
        m_limit = req.getLimit();
        //m_req = req;
        m_lineNo = 0;
    }
    
    public List<String> colNames() {
        AbstractList<String> res = new AbstractList<String>(){
            @Override
            public String get(int arg0) {
                return m_colNames.get(arg0);
            }

            @Override
            public int size() {
                return m_colNames.size();
            }
            
            @Override
            public String set(int index, String element) {
                m_colIndieces.put(element, index);
                return m_colNames.set(index, element);
            }
            
            @Override
            public boolean add(String element) {
                m_colIndieces.put(element, size());
                return m_colNames.add(element);
            }
        };
        
        return res;
    }
    public List<Object> values() {
        return m_values;
    }
    
    public int getRowsNumber(){
        return values().size()/colNames().size();
    }
    
    
    public String[] getColNames(){
        String[] res = new String[m_colNames.size()];
        return m_colNames.toArray(res);
    }
    public void setColNames(String colNames[]){
        List<String> list = Arrays.asList(colNames);
        setColNames(list);
    }
    
    public void setColNames(List<String> colNames){
        m_colNames = colNames;
        m_colIndieces.clear();
        int i=0;
        for (String colName : colNames) {
            m_colIndieces.put(colName, i++);
        }
    }

    public boolean addRow(){
        Object row[] = new Object[colNames().size()];
        Arrays.fill(row, null);
        return addRow(row);
    }
    
    public boolean addRow(Object ...row) {
        return addRow(Arrays.asList(row));
    }
    
    public boolean addRow(List<Object> row) {
        ApiAlgs.assertEquals(row.size(), m_colNames.size());
        boolean res=(m_limit!=0 && m_lineNo < m_startFrom+m_limit) || 
            m_limit==0;
        if(res && m_lineNo >= m_startFrom){
            m_values.addAll(row);
        }
        m_lineNo++;
        
        return res;
    }

    public List<Object> getValues() {
        return m_values;
    }
    
    public void setValues(List<Object> values) {
        m_values = values;
    }    
    
    /**
     * @param rowNum - zero based
     * @param columnName
     * @return cell value
     * @throws IndexOutOfBoundsException if rowNum is out of bounds or columnName not found
     */
    public Object getCell(int rowNum, String columnName) throws IndexOutOfBoundsException{
        return m_values.get(calValueIdx(rowNum, columnName));
    }
    public Object getCell(int rowNum, IName columnName) throws IndexOutOfBoundsException{
        return getCell(rowNum, columnName.getName());
    }
    public Long getIdCell(int rowNum, IName columnName) throws IndexOutOfBoundsException{
        return TFUtils.asLong(getCell(rowNum, columnName.getName()));
    }
    public Object getCell(int rowNum, int colNum) throws IndexOutOfBoundsException{
    	return m_values.get(calcValueIdx(rowNum, colNum));
    }
    
    public int calValueIdx(int rowNum, String columnName){
        Integer colNum = m_colIndieces.get(columnName);
        if(null == colNum)
            throw new IndexOutOfBoundsException("columnName: " + columnName); //$NON-NLS-1$
        return calcValueIdx(rowNum, colNum);
    }
    
    public int calcValueIdx(int rowNum, int colNum){
        if(colNum >= m_colNames.size())
            throw new IndexOutOfBoundsException(String.format("colNum: %d, size: %d", colNum, m_colNames.size())); //$NON-NLS-1$
        
        int idx = rowNum*m_colNames.size() + colNum;
        if(idx >= m_values.size()){
            int rNum = m_values.size() / m_colNames.size();
            if(rowNum >= rNum)
                throw new IndexOutOfBoundsException(String.format("rowNum: %d, size: %d", rowNum, rNum)); //$NON-NLS-1$
        }
        return idx;
    }
    
    public void setCell(int rowNum, int colNum, Object value) throws IndexOutOfBoundsException{
        m_values.set(calcValueIdx(rowNum, colNum), value);
    }
    
    public void setCell(int rowNum, String colName, Object value) throws IndexOutOfBoundsException{
        m_values.set(calValueIdx(rowNum, colName), value);
    }
    
    public void setCell(int rowNum, IName colName, Object value) throws IndexOutOfBoundsException{
        m_values.set(calValueIdx(rowNum, colName.getName()), value);
    }
    
    public void setStartFrom(int v) {
        m_startFrom = v;
    }

    public void setLimit(int v) {
        m_limit = v;
    }
    
    public void addRow(LongListResponse src, int rowNum){
        int idx = src.calcValueIdx(rowNum, 0);
        addRow(src.values().subList(idx, idx + colNames().size()));
    }
    
    public static LongListResponse createFromResSet(ResSet resSet, List<IName> extraColumns){
        
        ResultSetMetaData md;
        try {
            md = resSet.getResultSet().getMetaData();
            String colNames[] = new String[md.getColumnCount() + (null == extraColumns?0:extraColumns.size())];
            for (int i = 0; i < colNames.length; i++) {
                colNames[i] = md.getColumnName(i + 1).toLowerCase(); 
                
            }
            LongListResponse res = new LongListResponse(colNames);
            while(resSet.next()){
                Object row[] = new Object[colNames.length];
                for (int i = 0; i < md.getColumnCount(); i++) {
                    row[i] = resSet.getObject(i + 1);                    
                }
                res.addRow(row);
            }
            return res;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        
        return null;
        
    }
    

}
