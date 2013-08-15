/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

public abstract class BusMsg {
	private final Object m_data;

	public BusMsg(Object data) {
		m_data = data;
	}
	
	public Object getData(){
		return m_data;
	}

}
