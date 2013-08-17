/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

public abstract class BMMsg {
	private final Object m_data;

	public BMMsg(Object data) {
		m_data = data;
	}
	
	public Object getData(){
		return m_data;
	}

}
