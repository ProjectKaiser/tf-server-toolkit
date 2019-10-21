/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.triniforce.soap.TypeDef.ScalarDef;

public class ExternalDefLib implements IDefLibrary {

	public static class CharDef extends ScalarDef{
		private static final long serialVersionUID = 1097820399606277274L;

		public CharDef(Class type) {
			super("char", (Class) type, false);
		}
		
		@Override
		public Object valueOf(String value) {
			return Character.toChars(Integer.valueOf(value))[0];
		}
		
		@Override
		public String stringValue(Object v) {
			return Integer.toString(((Character)v).charValue());
		}
		
	}
	
	HashMap<Type, TypeDef> m_map = new HashMap<Type, TypeDef>();
	HashMap<Type, Boolean> m_xsdType = new HashMap<Type, Boolean>();
	
	public TypeDef get(Type type) {
		return m_map.get(type);
	}

	public TypeDef add(Type type) {
		TypeDef res = m_map.get(type); 
		if(null == res){
			if(char.class.equals(type))
				res = new CharDef((Class) type);
			if(null != res){
				m_map.put(type, res);
				m_xsdType.put(type, false);
			}
		}
		return res;
	}

	public Collection<? extends TypeDef> getDefs() {
		ArrayList<TypeDef> res = new ArrayList<TypeDef>();
		for(Map.Entry<Type, Boolean> entry : m_xsdType.entrySet()){
			if(!entry.getValue())
				res.add(m_map.get(entry.getKey()));
		}
		return res;
	}

	public void addDef(Type typ, TypeDef td, boolean isXsd){
		m_map.put(typ, td);
		m_xsdType.put(typ, isXsd);
	}
}
