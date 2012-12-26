/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.json.simple.parser.ParseException;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.JSONSerializerTest.Service001.Outter01;
import com.triniforce.soap.JSONSerializerTest.Service001.Real1;

public class JSONSerializerTest extends TFTestCase {
	
	@SoapInclude(extraClasses={
			Service001.Real1.class
	})
	static class Service001{
		public String method_001(int v1, int v2){
			return null;
		}

		public String method_002(int v1, int v2){
			return null;
		}
		
		interface IAbstract{}
		static class Real1  implements IAbstract{
			private String m_prop_001;

			public String getProp_001() {
				return m_prop_001;
			}

			public void setProp_001(String prop_001) {
				m_prop_001 = prop_001;
			}
		}
		public void method_003(IAbstract v){}
		
		public void method_004(Object[] v){}
		
		public void method_005(Map<String, String> map){}
		
		static class Outter01{
			private Object m_objValue;
			public Object getObjValue() {
				return m_objValue;
			}
			public void setObjValue(Object objValue) {
				m_objValue = objValue;
			}
		}
		
		public void method_006(Outter01 v){}
		
		public void method_007(String v){}
	}
	
	static class Prop01{
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public void testSerialize() throws IOException {
		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
		InterfaceDescription desc = gen.parse(null, Service001.class);
		JSONSerializer srz = new JSONSerializer();
		
		
		assertEquals( "{\"jsonrpc\":\"2.0\",\"params\":[124],\"method\":\"method_001\",\"id\":1}", serialize(srz, desc, 124));
		assertEquals( "{\"jsonrpc\":\"2.0\",\"params\":[515],\"method\":\"method_001\",\"id\":1}", serialize(srz, desc, 515));
		assertEquals( "{\"jsonrpc\":\"2.0\",\"params\":[515,634,\"turbo\"],\"method\":\"method_001\",\"id\":1}", serialize(srz, desc, 515, 634, "turbo"));
		assertEquals( "{\"jsonrpc\":\"2.0\",\"params\":[515,{\"~unique-id~\":\"0\",\"value\":\"val_0012\",\"class\":\"com.triniforce.soap.JSONSerializerTest$1\"}],\"method\":\"method_001\",\"id\":1}", serialize(srz, desc, 515, new Prop01(){{setValue("val_0012");}}));
	}

	private String serialize(JSONSerializer srz, InterfaceDescription desc,
			Object ... values) throws IOException {
		SOAPDocument soap = new InterfaceDescriptionGenerator.SOAPDocument();
		soap.m_method = "method_001";
		soap.m_args = values;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		srz.serialize(desc, soap, out);
		return out.toString("utf-8");
	}

	public void testDeserialize() throws IOException, ParseException {
		JSONSerializer srz = new JSONSerializer();
		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
		InterfaceDescription desc = gen.parse(null, Service001.class);
		SOAPDocument res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_001\",\"params\":[124],\"id\":1}"));
		assertNotNull(res);
		assertEquals(true, res.m_bIn);
		assertEquals("method_001", res.m_method);
		assertEquals(124, res.m_args[0]);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_002\",\"params\":[532, 2131],\"id\":1}"));
		assertEquals(true, res.m_bIn);
		assertEquals("method_002", res.m_method);
		assertEquals(532, res.m_args[0]);
		assertEquals(2131, res.m_args[1]);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_003\",\"params\":[{\"type\":\"Real1\", \"prop_001\":\"setter\"}],\"id\":1}"));
		Service001.Real1 obj = (Real1) res.m_args[0];
		assertEquals("setter",obj.getProp_001());
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_004\",\"params\":[[{\"type\":\"Real1\",\"prop_001\":\"setter\"}, {\"type\":\"Int\", \"value\":7652}]],\"id\":1}"));
		Object[] arg0 = (Object[]) res.m_args[0];
		obj = (Real1) arg0[0];
		assertNotNull(obj);
		assertEquals("setter",obj.getProp_001());

		Integer  i = (Integer) arg0[1];
		assertNotNull(i);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_005\",\"params\":[[" +
				"{\"key\":\"key_001\", \"value\":\"vvvv\"}, {\"key\":\"key_002\", \"value\":\"vvv2\"}]],\"id\":1}"));
		Map<String,String> map = (Map<String, String>) res.m_args[0];
		assertEquals("vvvv", map.get("key_001"));
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_004\",\"params\":[[\"string_value\", 362472]],\"id\":1}"));
		arg0 = (Object[]) res.m_args[0];
		assertEquals("string_value", arg0[0]);
		assertEquals(362472L, arg0[1]);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_006\",\"params\":[{\"objValue\":\"string_YT\"}],\"id\":1}"));
		Outter01 obj01 = (Outter01) res.m_args[0];
		assertEquals("string_YT", obj01.getObjValue());
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_006\",\"params\":[{\"objValue\":672}],\"id\":1}"));
		obj01 = (Outter01) res.m_args[0];
		assertEquals(672L, obj01.getObjValue());
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_006\",\"params\":[{\"objValue\":{\"type\":\"Short\", \"value\":67}}],\"id\":1}"));
		obj01 = (Outter01) res.m_args[0];
		assertEquals((short)67, obj01.getObjValue());


		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_007\",\"params\":[\"string cont\\\"aining [ :}\"],\"id\":1}"));
		assertEquals("string cont\"aining [ :}", res.m_args[0]);
		
	}

	public static InputStream source(String string) throws UnsupportedEncodingException {
		ByteArrayInputStream res = new ByteArrayInputStream(string.getBytes("utf-8"));
		return res;
	}

}
