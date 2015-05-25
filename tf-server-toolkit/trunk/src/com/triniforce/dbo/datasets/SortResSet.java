/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.triniforce.db.dml.BasicResSet;
import com.triniforce.db.dml.IResSet;
import com.triniforce.server.soap.CollectionViewRequest.DescField;

public class SortResSet extends BasicResSet implements IResSet {
	
	private List<String> m_columns;
	private int m_rowPointer=0;
	private List<Object[]> m_rows = new ArrayList<Object[]>();

	public SortResSet(List<String> columns) {
		m_columns = columns;
	}

	public void addRow(Object[] values){
		m_rows.add(values);
	}
	
	public void sort(List<Object> order){
		Collections.sort(m_rows, new ArrayRowComparator(createIndex(m_columns, order)));
	}
	
	
    /**
     * @param colNames   list of column names
     * @param orderBy    list of column for order
     * @return  list of indexes order columns in colNames, 
     *      if order column is DescField then index is -index-1
     */
    public static List<Integer> createIndex(List<String> colNames, List<Object> orderBy) {
        ArrayList<Integer> res = new ArrayList<Integer>(orderBy.size());
        for (Object obj : orderBy) {
            boolean bDesc = obj instanceof DescField;
            String orderColumn;
            if(bDesc){
                orderColumn = ((DescField)obj).getField();
            }
            else{
                orderColumn = (String) obj;
            }
            int colIndex = colNames.indexOf(orderColumn);
            if(bDesc)
                colIndex = -colIndex - 1;
                
            res.add(colIndex);
        }
        return res;
    }
	
    @Override
    public boolean first() {
        m_rowPointer = 0;
        return true;
    }
    
	public boolean next() {
		if(m_rowPointer == m_rows.size())
			return false;
		m_rowPointer ++;
		return true;
	}

	public Object getObject(int columnIndex) throws IndexOutOfBoundsException {
		return m_rows.get(m_rowPointer-1)[columnIndex-1];
	}

	public List<String> getColumns() {
		return m_columns;
	}

}
