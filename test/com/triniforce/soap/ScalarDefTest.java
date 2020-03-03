/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.StringWriter;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.TypeDef.ScalarDef;

public class ScalarDefTest extends TFTestCase {

	public void testSerialize() {
		ScalarDef sd = new TypeDef.ScalarDef(String.class);
		StringWriter w = new StringWriter();
		sd.serialize("123456bhf", w);
		assertEquals("123456bhf", w.toString());
	}
	
	public void testValueOf(){
		ScalarDef sd = new TypeDef.ScalarDef(Object.class);
		assertEquals("\u0002010F11C239", sd.valueOf("\u0002010F11C239"));
	}

}
