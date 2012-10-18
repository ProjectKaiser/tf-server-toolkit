/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.dml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.dml.Table.Row.State;
import com.triniforce.utils.IName;

public class Table {

    public static class UnknownFieldException extends RuntimeException{
        private static final long serialVersionUID = -3763274134105847608L;        
        String m_fName;
        public UnknownFieldException(String fName) {
            m_fName = fName;
        }
    }
    
    /**
     * User interface for read/write/delete data in table
     */
    public static class Row {
        public enum State {
            INTACT, INSERTED, UPDATED, DELETED, CANCELED;
            public boolean isInsertedOrUpdated(){
                return (UPDATED == this || INSERTED == this);
            }
            public boolean isDeleted(){
                return (DELETED == this );                
            }
        };

        private Table m_table;

        private int m_rowIdx;

        public Row(Table t, int idx) {
            m_table = t;
            m_rowIdx = idx;
        }

        /**
         * Set field value 
         * @param i - field index in table
         * @param v - value
         */
        public void setField(int i, Object v) {
            m_table.getRowInternal(m_rowIdx).setField(i, v);
        }

        /**
         * Set field value
         * @param fName - field name
         * @param v - value
         */
        public void setField(String fName, Object v) {
            setField(m_table.getFieldIndex(fName), v);
        }
        
        public void setField(IName col, Object v) {
            setField(m_table.getFieldIndex(col.getName()), v);
        }        

        /**
         * Get field value
         * @param i - field index in table
         * @return - value
         */
        public Object getField(int i) {
            return m_table.getRowInternal(m_rowIdx).getField(i);
        }

        /**
         * Get field value
         * @param fName - field name
         * @return - value
         */
        public Object getField(String fName) {
            return getField(m_table.getFieldIndex(fName));
        }

        public Object getField(IName col) {
            return getField(m_table.getFieldIndex(col.getName()));
        }        
        
        /**
         * Mark row as deleted
         */
        public void delete() {
            m_table.getRowInternal(m_rowIdx).delete();
        }

        /**
         * Get row state
         * @return row state
         */
        public State getState() {
            return m_table.getRowInternal(m_rowIdx).getState();
        }

        /**
         * Get field value before any changes
         * @param i - field index
         * @return - value
         */
        public Object getOriginalField(int i) {
            return m_table.getRowInternal(m_rowIdx).getOriginalField(i);
        }

        /**
         * Get field value before any changes
         * @param fName - field name
         * @return - value
         */
        public Object getOriginalField(String fName) {
            return getOriginalField(m_table.getFieldIndex(fName));
        }
        
        protected void accept(){            
            boolean bDeleted = getState() == Row.State.CANCELED || 
                    getState() == Row.State.DELETED;
            m_table.acceptRow(m_rowIdx);
            if(bDeleted){
                m_table = null;
                m_rowIdx = -1;
            }   
        }

        public int getIndex() {
            return m_rowIdx;
        }
    }

    /**
     * Class for store and operate by data in table
     */
    private static class RowInternal {

        protected Object m_f[];
        
        protected ArrayList<Update> m_upds;
        
        protected State m_state;

        protected class Update{
            
            public int m_col;
            public Object m_val;
            
            public Update(int column, Object v){
                m_col = column;
                m_val = v; 
            }
        }

        public RowInternal(Object v[], State s) {
            m_f = v;
            m_state = s;
        }
        
        private int searchUpdate(int pos){
            int i;
            for (i = 0; i < m_upds.size(); i++) {
                int col = m_upds.get(i).m_col;
                if (col == pos)
                    return i;
                else if(col > pos){
                    i++;
                    return -i;
                }
            }
            i++;
            return -i;
        }

        public Object getOriginalField(int i) {
            return m_f[i];
        }

        public State getState() {
            return m_state;
        }

        public void setField(int i, Object v) {
            switch(getState()){
            case INTACT:
                m_state = State.UPDATED;
                m_upds = new ArrayList<Update>();
                m_upds.add(new Update(i, v));
                break;
            case UPDATED:
                int pos = searchUpdate(i);
                if(pos < 0){
                    pos ++;
                    m_upds.add(-pos, new Update(i, v));
                }
                else
                    m_upds.get(pos).m_val = v;                    
                break;
            case INSERTED:
                m_f[i] = v;
                break;
            }
        }

        public Object getField(int i) {
            if(m_state.equals(State.UPDATED)){
                int pos = searchUpdate(i);                
                if(pos >= 0)
                    return m_upds.get(pos).m_val; 
            }
            return m_f[i];
        }

        public void delete() {
            switch(m_state){
            case INTACT:
            case UPDATED:
                m_state = State.DELETED;
                break;
            case INSERTED:
                m_state = State.CANCELED;
                break;
            }
        }
        
        protected void accept(){
            if(m_upds != null){
                for (Update update : m_upds) {
                    m_f[update.m_col] = update.m_val;
                }
            }
            m_state = State.INTACT;
            m_upds = null;
        }
    }

//    public class UniqueIndex{
//    	Map<Object, Integer> m_map = new HashMap<Object, Integer>();
//    	public UniqueIndex(int fieldIndex) {
//    		int i=0;
//    		for(RowInternal r : m_rows){
//				m_map.put(r.getField(fieldIndex), i++);
//			}
//		}
//
//		public Row get(Object key){
//			Integer idx = m_map.get(key);
//			ApiAlgs.assertNotNull(idx, key.toString());
//			return getRow(idx);
//    	}
//    }
    
    private ArrayList<FieldDef> m_fields;

    private ArrayList<RowInternal> m_rows;

    private HashMap<String, Integer> m_fieldPos;
    
//    ArrayList<UniqueIndex> m_indexes = new ArrayList<UniqueIndex>();

    public Table() {
        m_fields = new ArrayList<FieldDef>();
        m_rows = new ArrayList<RowInternal>();
        m_fieldPos = new HashMap<String, Integer>();
    }

    
    protected void acceptRow(int idx) {
        RowInternal r = getRowInternal(idx);
        switch(r.getState()){
        case DELETED:
        case CANCELED:
            m_rows.remove(idx);
            break;
        case UPDATED:
        case INSERTED:
            r.accept();
            break;
        }            
    }
    
    protected void acceptRows(List<Integer> indieces){
        ArrayList<RowInternal> delItems = new ArrayList<RowInternal>();                 
        for (int i=0; i<indieces.size();i++) {
            RowInternal r = m_rows.get(indieces.get(i));
            switch(r.getState()){
            case DELETED:
            case CANCELED:
                delItems.add(r);
                break;
            case UPDATED:
            case INSERTED:
                r.accept();
                break;
            default:
                break;
            }            
        }
        if(delItems.size()>0)
            m_rows.removeAll(delItems);
    }

    protected RowInternal getRowInternal(int idx) {
        return m_rows.get(idx);
    }

    private int getFieldIndex(String fName) {        
        Integer idx = m_fieldPos.get(fName.toUpperCase());
        if(idx == null)
            throw new UnknownFieldException(fName);
        return idx;
    }

    /**
     * Adds a column of the specified type
     * @param f field definition for the new column
     */
    public void addColumn(FieldDef f) {        
        m_fieldPos.put(f.getName().toUpperCase(), m_fields.size());
        m_fields.add(f);        
        for (int iRow = 0; iRow < m_rows.size(); iRow++) {
            RowInternal oldRow = m_rows.get(iRow);
            Object v[] = new Object [m_fields.size()];
            int i;
            for (i = 0; i < m_fields.size()-1; i++) {
                v[i] = oldRow.getField(i);
            }
            RowInternal newRow = new RowInternal(v, oldRow.getState());
            newRow.setField(i, f.getDefaultValue());
            m_rows.set(iRow, newRow);
        }
    }

    /**
     * Populates table from given resultset
     * @param rs resultset to populate table from
     * @throws SQLException
     */
    public void populate(ResultSet rs) throws SQLException {
        //rs.beforeFirst();
        while (rs.next()){
            Object v[] = new Object[m_fields.size()];
            int i=0;
            for (FieldDef f : m_fields) {
                if(f.getType().equals(FieldDef.ColumnType.BLOB))
                    v[i++] = rs.getBlob(f.getName());
                else
                    v[i++] = rs.getObject(f.getName());
            }
            m_rows.add(new RowInternal(v, State.INTACT));
        }
    }

    /**
     * @return number of rows in the table
     */
    public int getSize() {
        return m_rows.size();
    }

    /**
     * @param idx index of row
     * @return row by specified index
     */
    public Row getRow(int idx) {
        return new Row(this, idx);
    }

    /**
     * Appends a new row to table
     * @return new row
     */
    public Row newRow() {
        Object v[] = new Object[m_fields.size()];
        int i=0;
        for (FieldDef f : m_fields) {
            v[i++] = f.getDefaultValue();
        }
        RowInternal r = new RowInternal(v, State.INSERTED);
        int rowIdx = m_rows.size();
        m_rows.add(r);
        return new Row(this, rowIdx);
    }

    /**
     * Fields defined in table 
     * @return fields collection
     */
    public List<FieldDef> getFieldDefs() {
        return Collections.unmodifiableList(m_fields);
    }
    
    /**
     * Accept all changes after data sync
     */
    public void acceptChanges() {
        ArrayList<RowInternal> delItems = new ArrayList<RowInternal>();                 
        for (RowInternal r : m_rows) {
            switch(r.getState()){
            case DELETED:
            case CANCELED:
                delItems.add(r);
                break;
            case UPDATED:
            case INSERTED:
                r.accept();
                break;
            default:
                break;
            }            
        }
        if(delItems.size()>0)
            m_rows.removeAll(delItems);
    }

    public boolean containField(String fieldName) {
        for (FieldDef def : getFieldDefs()) {
            if(def.getName().equals(fieldName))
                return true;
        }
        return false;
    }

//
//	public UniqueIndex createIndex(String fName) {
//		UniqueIndex res = new UniqueIndex(getFieldIndex(fName));
//		m_indexes.add(res);
//		return res;
//		
//	}
}
