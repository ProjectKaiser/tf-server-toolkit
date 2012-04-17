/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

public class SetAutoIncFieldOperation extends TableOperation {
	
	private String m_name;
	private String m_genName;
	private String m_fname;

	public SetAutoIncFieldOperation(String opName, String genName, String fname) {
		m_name = opName;
		m_genName = genName;
		m_fname = fname;
	}

	@Override
	public String getName() {
		return m_name;
	}
	
	public String getGeneratorName(){
		return m_genName;
	}
	
	public String getFieldName(){
		return m_fname;
	}

	@Override
	public TableOperation getReverseOperation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersionIncrease() {
		// TODO Auto-generated method stub
		return 0;
	}

}
