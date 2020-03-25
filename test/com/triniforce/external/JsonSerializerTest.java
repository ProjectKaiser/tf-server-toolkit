/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.external;

import java.lang.reflect.Field;
import java.util.Locale;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.triniforce.db.test.TFTestCase;

import net.sf.sojo.interchange.json.JsonSerializer;

public class JsonSerializerTest extends TFTestCase {

	public static class TestObject{
		String m_id;
		String m_i001;
		
		public TestObject() {
			m_id = "88912";
			m_i001 = "some string";
		}
		
		public String getId() {
			return m_id;
		}
		public void setId(String id) {
			m_id = id;
		}
		public String getI001() {
			return m_i001;
		}
		public void setI001(String i001) {
			m_i001 = i001;
		}
		
	}
	
	public void testSerializeSOJO(){
		Locale loc = Locale.getDefault();
		try{
			Locale.setDefault(new Locale("tr", "TR"));
			JsonSerializer srz = new JsonSerializer();
			Object buf = srz.serialize(new TestObject());
			trace(buf);
			Locale.setDefault(Locale.ENGLISH);
			TestObject res = (TestObject) srz.deserialize(buf);
			assertEquals("88912", res.getId());
			assertEquals("some string", res.getI001());
		}finally{
			Locale.setDefault(loc);
		}
	}
	
	static class FNS implements FieldNamingStrategy{

		@Override
		public String translateName(Field f) {
			return "test_"+f.getName();
		}
		
	}
	
	public void testSerializeGson(){
		Locale loc = Locale.getDefault();
		try{
			Locale.setDefault(new Locale("tr", "TR"));
			GsonBuilder b = new GsonBuilder();
			b.setFieldNamingStrategy(new FNS());
			Gson srz = b.create();
			Object buf = srz.toJson(new TestObject());
			trace(buf);
			Locale.setDefault(Locale.ENGLISH);
			TestObject res = (TestObject) srz.fromJson((String) buf, TestObject.class);
			assertEquals("88912", res.getId());
			assertEquals("some string", res.getI001());
		}finally{
			Locale.setDefault(loc);
		}		
	}
}
