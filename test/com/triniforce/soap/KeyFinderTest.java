/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.JSONSerializer.KeyFinder;

public class KeyFinderTest extends TFTestCase {
	static class C1{
		private String p1;
		@Override
		public boolean equals(Object obj) {
			return ((C1)obj).getP1().equals(p1);
		}
		
		public C1() {
		}
		public C1(String v){
			setP1(v);
		}
		public String getP1() {
			return p1;
		}
		public void setP1(String p1) {
			this.p1 = p1;
		}
		
		@Override
		public String toString() {
			return "p1:"+p1;
		}
	};
	interface IService{
		void method_0001(String value, String[] value2, List<C1> value3);
	}

	public void testStartArray() throws ParseException, IOException {
		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
		InterfaceDescription desc = gen.parse(null, IService.class); 
		SAXHandler sh = new SAXHandler(desc);
		KeyFinder kf = new JSONSerializer.KeyFinder(sh, desc);
		kf.startObject();
		kf.startObjectEntry("method");
		kf.primitive("method_0001");
		kf.endObjectEntry();
		kf.startObjectEntry("params");
		
		kf.startArray();
		kf.primitive("first argument");
		kf.startArray();
		kf.primitive("second arg in array");
		kf.primitive("third arg in array");
		kf.endArray();
		
		kf.startArray();
		kf.startObject();
		kf.startObjectEntry("p1");
		kf.primitive("1");
		kf.endObjectEntry();
		kf.endObject();
		kf.startObject();
		kf.startObjectEntry("p1");
		kf.primitive("2");
		kf.endObjectEntry();
		kf.endObject();
		kf.endArray();
		
		kf.endArray();
		kf.endObjectEntry();
		kf.endObject();
		
		Object arg0 = sh.m_args[0];
		assertNotNull(arg0);
		assertEquals("first argument", (String)arg0);
		
		String[] arg1 = (String[]) sh.m_args[1];
		assertEquals(Arrays.asList("second arg in array","third arg in array"), Arrays.asList(arg1));
		
		assertEquals(Arrays.asList(new C1("1"), new C1("2")), sh.m_args[2]);
	}

}
