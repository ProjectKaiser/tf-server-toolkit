/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import net.sf.sojo.core.UniqueIdGenerator;
import net.sf.sojo.core.filter.ClassPropertyFilter;
import net.sf.sojo.core.filter.ClassPropertyFilterHandlerImpl;
import net.sf.sojo.interchange.json.JsonSerializer;

import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.JSONSerializer.JsonRpcError.Error;

public class JSONSerializer {
	static class JsonRpc{
		private String m_jsonrpc;
		Object m_id;
		
		public JsonRpc(String jsonrpc, Object id) {
			setJsonrpc(jsonrpc);
			m_id = id;
		}
		public String getJsonrpc() {
			return m_jsonrpc;
		}
		public void setJsonrpc(String jsonrpc) {
			m_jsonrpc = jsonrpc;
		}
		public Object getId() {
			return m_id;
		}
		public void setId(Object id) {
			m_id = id;
		}
	}
	
	static class JsonRpcMessage extends JsonRpc{
		String m_method;
		Object[] m_params;
		public JsonRpcMessage(String jsonrpc, String method, Object[] params,
				Object id) {
			super(jsonrpc, id);
			m_method = method;
			m_params = params;
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
	}
	
	static class JsonRpcError extends JsonRpc{
		static class Error{
			private int m_code;
			private String m_message;
			
			public Error(int code, String msg) {
				setCode(code);
				setMessage(msg);
			}

			public int getCode() {
				return m_code;
			}

			public void setCode(int code) {
				m_code = code;
			}

			public String getMessage() {
				return m_message;
			}

			public void setMessage(String message) {
				m_message = message;
			}
		}
		private Error m_error;
		public JsonRpcError(String jsonrpc, Object id, Error error) {
			super(jsonrpc, id);
			setError(error);
		}
		public Error getError() {
			return m_error;
		}
		public void setError(Error error) {
			m_error = error;
		}
		
		
	}

	private JsonSerializer js;
	
	public JSONSerializer() {
		js = new JsonSerializer();
		final ClassPropertyFilter pf = new ClassPropertyFilter(JsonRpc.class);
		pf.addProperty(UniqueIdGenerator.UNIQUE_ID_PROPERTY);
		pf.addProperty("class");
		ClassPropertyFilterHandlerImpl handler = new ClassPropertyFilterHandlerImpl();
		handler.addClassPropertyFilter(pf);
		js.setClassPropertyFilterHandler(handler);
		
	}
	
	public void serialize(InterfaceDescription desc, SOAPDocument soap, OutputStream out) throws IOException{
		JsonRpc obj = new JsonRpcMessage("2.0", soap.m_method, soap.m_args, 1);
		serializeObject(js, obj, out);
	}
	
	private void serializeObject(JsonSerializer js, Object obj, OutputStream out) throws IOException {
		Object res = js.serialize(obj, new String[]{UniqueIdGenerator.UNIQUE_ID_PROPERTY, "class"});
		String str = res.toString();
		out.write(str.getBytes("utf-8"));
	}

	public SOAPDocument deserialize(InterfaceDescription desc, InputStream source) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(source));
		String str = reader.readLine();
		Map<String, Object> map = (Map<String, Object>) js.deserialize(str);
		SOAPDocument res = new SOAPDocument();
		res.m_method = (String) map.get("method");
		List params = (List) map.get("params");
		res.m_args = params.toArray();
		return res;
	}

	public void serializeError(InterfaceDescription desc, Throwable t, OutputStream out) throws IOException {
		JsonSerializer js = new JsonSerializer();
		JsonRpc obj = new JsonRpcError("2.0",1, new Error(0, null));
		serializeObject(js, obj, out);
	}
}
