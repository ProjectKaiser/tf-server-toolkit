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
import com.triniforce.server.soap.WhereExpr;

public class PipeResSet extends BasicResSet implements IResSet, IRow{
	
	private IResSet m_src;
	private int m_srcSize;
	private List<String> m_columns;
	private WhereCalc m_where;
	private List<Object> m_ffCalls = new ArrayList<Object>();
	private FieldFunctionCalc m_ffCalc = new FieldFunctionCalc();
		
	public PipeResSet(IResSet src){
		m_src = src;
		m_columns = m_src.getColumns();
		m_srcSize = m_columns.size();
		
	}
	
	@Override
	public boolean isRowBeg() {
		return m_src.isRowBeg();
	}
	
	@Override
	public boolean first() {
	    return m_src.first();
	}

	public boolean next() {
		while(m_src.next()){
			m_ffCalls = m_ffCalc.calc(this);
			if(!isFiltered())
				return true;
		}
		return false;
	}

	private boolean isFiltered() {
		return null==m_where ? false : !m_where.calc(this);
	}

	public Object getObject(int columnIndex) throws IndexOutOfBoundsException {
		Object res;
		if(columnIndex > m_srcSize){
			res = m_ffCalls.get(columnIndex - 1 - m_srcSize);
		}
		else
			res = m_src.getObject(columnIndex);
		return res;
	}

	public List<String> getColumns() {
		return m_columns;
	}

	public void addFilter(String column, Object value) {
		if(null == m_where)
			m_where = new WhereCalc();
		m_where.addExpr(m_columns, column, value);
	}
	
	public void addFilter(List<WhereExpr> where){
		m_where = new WhereCalc(m_columns, where);
	}

	int columnIndex(String column) {
		return m_columns.indexOf(column)+1;
	}

	public void addFieldFunction(String column, String srcColumn, FieldFunction ff) {
		if(m_columns.contains(column))
			throw new EColumnAlreadyAdded(column);
		m_columns = new ArrayList<String>(m_columns);
		m_columns.add(column);
		m_ffCalc.addFieldFunction(m_columns, srcColumn, ff);
	}

}
