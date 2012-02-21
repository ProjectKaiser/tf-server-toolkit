/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.lang.reflect.Type;
import java.util.List;

public class InterfaceOperationDescription {
	
	public static class NamedArg{
		private String m_name;
		private Type m_type;
		public NamedArg(String name, Type type) {
			m_name = name;
			m_type = type;
		}
		public String getName() {
			return m_name;
		}
		public void setName(String name) {
			m_name = name;
		}
		public Type getType() {
			return m_type;
		}
		public void setType(Type type) {
			m_type = type;
		}
	}

	public InterfaceOperationDescription(){}
	
	public InterfaceOperationDescription(String name, List<NamedArg> args, NamedArg res) {
		m_name = name;
		m_args = args;
		m_result = res;
	}
	
	private String m_name;
	private List<NamedArg> m_args;
	private NamedArg m_result;
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		m_name = name;
	}
	public List<NamedArg> getArgs() {
		return m_args;
	}
	public void setArgs(List<NamedArg> args) {
		m_args = args;
	}
	public NamedArg getResult() {
		return m_result;
	}
	public void setResult(NamedArg result) {
		m_result = result;
	}
	
	
}
