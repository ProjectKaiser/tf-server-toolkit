/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

public class BM {
	private final Object m_data;

	public BM(Object data) {
		m_data = data;
	}
	
	public Object getData(){
		return m_data;
	}

}
