/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

public class BMException extends BM{
	private final BMCall m_call;

	public BMException(BMCall call, Exception e){
		super(e);
		m_call = call;
	}

	public Exception getException() {
		return (Exception) getData();
	}

	public BMCall getCall() {
		return m_call;
	}
}
