/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sojo.core.UniqueIdGenerator;
import net.sf.sojo.core.filter.ClassPropertyFilter;
import net.sf.sojo.core.filter.ClassPropertyFilterHandlerImpl;
import net.sf.sojo.interchange.json.JsonSerializer;

import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;

public class JSONSerializer {
	static class JsonRpc{
		private String m_jsonrpc;
		String m_method;
		Object[] m_params;
		Object m_id;
		
		public JsonRpc(String jsonrpc, String method, Object[] params, Object id) {
			setJsonrpc(jsonrpc);
			m_method = method;
			m_params = params;
			m_id = id;
		}
		public String getJsonrpc() {
			return m_jsonrpc;
		}
		public void setJsonrpc(String jsonrpc) {
			m_jsonrpc = jsonrpc;
		}
		public String getMethod() {
			return m_method;
		}
		public void setMethod(String method) {
			m_method = method;
		}
		public Object[] getParams() {
			return m_params;
		}
		public void setParams(Object[] params) {
			m_params = params;
		}
		public Object getId() {
			return m_id;
		}
		public void setId(Object id) {
			m_id = id;
		}
	}
	
	public void serialize(InterfaceDescription desc, SOAPDocument soap, OutputStream out) throws IOException{
		JsonSerializer js = new JsonSerializer();
		final ClassPropertyFilter pf = new ClassPropertyFilter(JsonRpc.class);
		pf.addProperty(UniqueIdGenerator.UNIQUE_ID_PROPERTY);
		pf.addProperty("class");
		ClassPropertyFilterHandlerImpl handler = new ClassPropertyFilterHandlerImpl();
		handler.addClassPropertyFilter(pf);
		js.setClassPropertyFilterHandler(handler);
		JsonRpc obj = new JsonRpc("2.0", soap.m_method, soap.m_args, 1);
		Object res = js.serialize(obj, new String[]{UniqueIdGenerator.UNIQUE_ID_PROPERTY, "class"});
		String str = res.toString();
		out.write(str.getBytes("utf-8"));
	}
	
	public SOAPDocument deserialize(InterfaceDescription desc, InputStream source){
		
		return null;
	}

	public void serializeError(InterfaceDescription desc, Throwable t,
			ByteArrayOutputStream out) {
		// TODO Auto-generated method stub
		
	}
}
