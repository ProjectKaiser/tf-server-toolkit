/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

public class BMException {
	private final BMCall m_call;
	private final Exception m_e;

	public BMException(BMCall call, Exception e){
		m_call = call;
		m_e = e;
	}

	public Exception getException() {
		return m_e;
	}

	public BMCall getCall() {
		return m_call;
	}
}
