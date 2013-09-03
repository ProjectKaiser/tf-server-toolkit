/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.dml.BasicResSet;
import com.triniforce.db.dml.IResSet;

public class TruncResSet extends BasicResSet implements IResSet {
	
	public static class EColumnNotFound extends RuntimeException{
		private static final long serialVersionUID = 529066088613886774L;

		public EColumnNotFound(String column) {
			super(column);
		}
	}
	
	private List<String> m_columns = new ArrayList<String>();
	private List<Integer> m_columnIndieces = new ArrayList<Integer>();
	private IResSet m_src;
	private int m_lineNo = -1;
	private int m_from = 0;
	private int m_to;
		

	public TruncResSet(IResSet src) {
		m_src = src;
	}
	
	public void addColumn(String column){
		int idx;
		if((idx = m_src.getColumns().indexOf(column)) < 0)
			throw new EColumnNotFound(column);
		m_columns.add(column);
		m_columnIndieces.add(idx+1);
	}
	
	public void setFromBorder(int from){
		m_from = from;
	}
	
	public void setToBorder(int to){
		m_to = to;
	}
	
    @Override
    public boolean first() {
        m_lineNo = -1;
        return m_src.first();
    }
	
	public boolean next() {
		do{
			m_lineNo++;
			if(0!=m_to && m_lineNo >= m_to)
				return false;
			if(!m_src.next())
				return false;
			
		}while(m_lineNo < m_from);
		return true;
	}

	public Object getObject(int columnIndex) throws IndexOutOfBoundsException {
		return m_src.getObject(m_columnIndieces.get(columnIndex - 1));
	}

	public List<String> getColumns() {
		return m_columns;
	}

}
