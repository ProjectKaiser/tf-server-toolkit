/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.triniforce.server.soap.FieldFunctionRequest;

public class FieldFunctionCalc {

	static class FieldFunctionCall {
		FieldFunction m_ff;
		int m_ffReqField;

		public FieldFunctionCall(FieldFunction fieldFunction, int indexOf) {
			m_ff = fieldFunction;
			m_ffReqField = indexOf;
		}

		public Object execFunction(IRow row) {
			return m_ff.exec(row.getObject(m_ffReqField));
		}
	}
	
	private List<FieldFunctionCall> m_ffCalls = new ArrayList<FieldFunctionCall>();

	public FieldFunctionCalc(){}
	public FieldFunctionCalc(List<String> colNames,
			List<FieldFunctionRequest> ffr, List<FieldFunction> ffs) {
		Iterator<FieldFunctionRequest> iFfr = ffr.iterator();
		for (FieldFunction fieldFunction : ffs) {
			addFieldFunction(colNames, iFfr.next().getFieldName(), fieldFunction);
		}
	}
	
	public void addFieldFunction(List<String> columns, String srcColumn, FieldFunction ff) {
		int idx = columns.indexOf(srcColumn);
		if(idx < 0)
			throw new EDSException.ECVRColumnException.EWrongColumnName(srcColumn);
		m_ffCalls.add(new FieldFunctionCall(ff, idx+1));
	}

	List<Object> calc(IRow row) {
		ArrayList<Object> res = new ArrayList<Object>();
		for (FieldFunctionCall ffc : m_ffCalls) {
			res.add(ffc.execFunction(row));
		}
		return res;
	}
}
