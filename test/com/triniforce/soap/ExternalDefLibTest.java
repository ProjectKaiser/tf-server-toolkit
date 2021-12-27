/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.TypeDef.ScalarDef;

public class ExternalDefLibTest extends TFTestCase {

	
	@Override
	public void test() throws Exception {
		ExternalDefLib lib = new ExternalDefLib();
		ScalarDef sd  = (ScalarDef) lib.add(char.class);
		assertNotNull(sd);
		assertEquals('a', sd.valueOf("97"));
		assertEquals("102", sd.stringValue('f', new InterfaceDescription(), TypeDef.ContType.XML));
		
	}
}
