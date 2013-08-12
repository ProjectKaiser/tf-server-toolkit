/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
		m_args = new ArrayList<NamedArg>(args);
		m_result = res;
	}
	
	private String m_interface;
	private String m_pkgPrefix;
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
		m_args = new ArrayList<NamedArg>(args);
	}
	public NamedArg getResult() {
		return m_result;
	}
	public void setResult(NamedArg result) {
		m_result = result;
	}

	public String getInterface() {
		return m_interface;
	}

	public void setInterface(String interface1) {
		m_interface = interface1;
	}

	public String getPkgPrefix() {
		return m_pkgPrefix;
	}

	public void setPkgPrefix(String pkgPrefix) {
		m_pkgPrefix = pkgPrefix;
	}
	
	public void addNamedArg(int pos, String name, Type type){
		m_args.add(Math.min(pos, m_args.size()), new NamedArg(name, type));
	}
	
	@Override
	public String toString() {
		return m_name;
	}
	
}
