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
		InterfaceDescription desc = new InterfaceDescription();
		{
			ScalarDef sd = new TypeDef.ScalarDef(String.class);
			StringWriter w = new StringWriter();
			sd.serialize("123456bhf", w, desc, TypeDef.ContType.JSON);
			assertEquals("123456bhf", w.toString());
			
			StringWriter w2 = new StringWriter();
			sd.serialize("{\"key\":\"va<>lue\"}", w2, desc, TypeDef.ContType.JSON);
			assertEquals("{\\\"key\\\":\\\"va<>lue\\\"}", w2.toString());

		}
		{
			ScalarDef sd = new TypeDef.ScalarDef(String.class);
			StringWriter w = new StringWriter();
			sd.serialize("7776\u0014\u00151245", w, desc, TypeDef.ContType.XML);
			assertEquals("77761245", w.toString());
		}
		
		
	}
	
	public void testValueOf(){
		ScalarDef sd = new TypeDef.ScalarDef(Object.class);
		assertEquals("\u0002010F11C239", sd.valueOf("\u0002010F11C239"));
	}

}
