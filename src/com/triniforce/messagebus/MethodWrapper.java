/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

import java.lang.reflect.Method;

import com.triniforce.utils.IMessageHandler;

public class MethodWrapper implements IMessageHandler{
	
	private final Object m_o;
	private final Method m_m;

	public MethodWrapper(Object o, Method m){
		m_o = o;
		m_m = m;
	}

	@Override
	public void onMessage(Object arg) throws Exception {
		m_m.invoke(m_o, arg);
	}
	
	@Override
	public String toString() {
		return m_o.toString() + "." + m_m.getName() +"(msg)";
	}

}
