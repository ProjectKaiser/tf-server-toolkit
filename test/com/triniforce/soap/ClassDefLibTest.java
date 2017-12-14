/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDefLibCache.ClassDefLib;

public class ClassDefLibTest extends TFTestCase {

	public void testAdd() {
		ClassParser parser = new ClassParser(getClass().getPackage(), Collections.EMPTY_LIST);
		TypeDefLibCache parent = new TypeDefLibCache(parser, Collections.EMPTY_LIST);
		HashMap<Type, TypeDef> map = new HashMap<Type, TypeDef>();
		
		ClassDefLib lib = new TypeDefLibCache.ClassDefLib(parser, parent, map, parent);
		
		ClassDef cd1 = (ClassDef) lib.add(com.triniforce.soap.testpkg_01.DuplicatedName.class);
		ClassDef cd2 = (ClassDef) lib.add(com.triniforce.soap.testpkg_02.DuplicatedName.class);
		
		assertEquals("DuplicatedName", cd1.getName());
		assertEquals("DuplicatedName1", cd2.getName());
		
	}

}
