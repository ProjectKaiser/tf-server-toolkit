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

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;

public class JSONSerializerTest extends TFTestCase {
	
	static class Service001{
		public String method_001(int v1, int v2){
			return null;
		}

		public String method_002(int v1, int v2){
			return null;
		}
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
		
		assertEquals("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32601, \"message\": \"Procedure not found.\"}, \"id\": 10}", error(srz,desc, new Exception("alarm error")));
	}

	private String error(JSONSerializer srz, InterfaceDescription desc, Throwable t) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		srz.serializeError(desc, t, out);
		return out.toString("utf-8");
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

	public void testDeserialize() throws IOException {
		JSONSerializer srz = new JSONSerializer();
		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
		InterfaceDescription desc = gen.parse(null, Service001.class);
		SOAPDocument res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"params\":[124],\"method\":\"method_001\",\"id\":1}"));
		assertNotNull(res);
		assertEquals(false, res.m_bIn);
		assertEquals("method_001", res.m_method);
		assertEquals(124, res.m_args[0]);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"params\":[532, 2131],\"method\":\"method_002\",\"id\":1}"));
		assertEquals(false, res.m_bIn);
		assertEquals("method_002", res.m_method);
		assertEquals(532, res.m_args[0]);
		assertEquals(2131, res.m_args[1]);
	}

	public static InputStream source(String string) throws UnsupportedEncodingException {
		ByteArrayInputStream res = new ByteArrayInputStream(string.getBytes("utf-8"));
		return res;
	}

}
