/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.util.List;

import com.triniforce.db.qbuilder.QSelect;

public class ViewDef {

	private String m_name;
	private List<String> m_columns;
	private QSelect m_qSel;

	public ViewDef(String name, List<String> columns, QSelect qSel) {
		m_name = name;
		m_columns = columns;
		m_qSel = qSel;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean bComma = false;
		for(String col : m_columns){
			if(bComma)
				buf.append(", ");
			buf.append(col);
			bComma = true;
		}
		return String.format("create view %s (%s) as %s", m_name, buf.toString(), m_qSel);
	}
}
