/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

import java.util.HashSet;
import java.util.Locale;

public class UniqueNameGenerator{

	private HashSet<String> m_set;

	public UniqueNameGenerator() {
		m_set = new HashSet<String>();
	}
	
	public String generate(String template){
		if(contains(template)){
			String tmpValue; 
	        int i=1;
	        do{
	        	tmpValue = template + Integer.toString(i++);
	        }while(contains(tmpValue));
			template = tmpValue;
		}
		m_set.add(template.toLowerCase(Locale.ENGLISH));
		return template;
	}

	public boolean contains(String value) {
		return m_set.contains(value.toLowerCase(Locale.ENGLISH));
	}
	
}
