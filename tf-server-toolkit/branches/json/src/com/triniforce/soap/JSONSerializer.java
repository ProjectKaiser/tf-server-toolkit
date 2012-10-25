/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Stack;

import net.sf.sojo.core.UniqueIdGenerator;
import net.sf.sojo.core.filter.ClassPropertyFilter;
import net.sf.sojo.core.filter.ClassPropertyFilterHandlerImpl;
import net.sf.sojo.interchange.json.JsonSerializer;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.JSONSerializer.JsonRpcError.Error;
import com.triniforce.soap.JSONSerializer.KeyFinder.Element.Type;
import com.triniforce.soap.SAXHandler.CurrentObject;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.utils.ApiAlgs;

public class JSONSerializer {
	static class JsonRpc{
		private String m_jsonrpc;
		Integer m_id;
		
		public JsonRpc(String jsonrpc, Integer id) {
			setJsonrpc(jsonrpc);
			m_id = id;
		}
		public String getJsonrpc() {
			return m_jsonrpc;
		}
		public void setJsonrpc(String jsonrpc) {
			m_jsonrpc = jsonrpc;
		}
		public Integer getId() {
			return m_id;
		}
		public void setId(Integer id) {
			m_id = id;
		}
	}
	
	static class JsonRpcMessage extends JsonRpc{
		String m_method;
		Object[] m_params;
		public JsonRpcMessage(String jsonrpc, String method, Object[] params,
				Integer id) {
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
		public JsonRpcError(String jsonrpc, Integer id, Error error) {
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
		serializeObject(obj, out);
	}
	
	public void serializeObject(Object obj, OutputStream out) throws IOException {
		Object res = js.serialize(obj, new String[]{UniqueIdGenerator.UNIQUE_ID_PROPERTY, "class"});
		String str = res.toString();
		out.write(str.getBytes("utf-8"));
	}
	
	static class KeyFinder implements ContentHandler{
		enum State {Method, Arguments, Finit};
		
		private SAXHandler m_handler;
		private String m_method = null;
		private Object m_value;
		
		private String m_entry;
		int m_argIdx = 0;
		private State m_state;
		private boolean m_bSetType=false;
		private boolean m_bSetScalarValue=false;
		
		static final HashSet<String> SCALARS = new HashSet<String>(ScalarDef.scalarNames());
		
		static class Element{
			enum Type{Entry, Array, Object};
			String m_name;
			Type m_type;
			public Element(Type type, String name) {
				m_type = type;
				m_name = name;
			}
			@Override
			public boolean equals(Object obj) {
				Element other = (Element)obj;
				return m_type.equals(other.m_type) && m_name.equals(other.m_name);
			}
			
			@Override
			public String toString() {
				return String.format("%c, %s", m_type.toString().charAt(0), m_name);
			}
			
		}
		
		Stack<Element> m_stk = new Stack<Element>();

		public KeyFinder(SAXHandler handler) {
			m_handler = handler;
		}



		public void startJSON() throws ParseException, IOException {
			ApiAlgs.getLog(this).trace("startJSON");
		}
		public void endJSON() throws ParseException, IOException {
			ApiAlgs.getLog(this).trace("endJSON");

		}

		public boolean startObject() throws ParseException, IOException {
			startStackElement(new Element(Element.Type.Object, ""));
			m_bSetType=false;
			return true;
		}
		public boolean endObject() throws ParseException, IOException {
			endStackElement(Element.Type.Object);
			return true;
		}
		
		public boolean startObjectEntry(String arg0) throws ParseException,
				IOException {
			ApiAlgs.getLog(this).trace("startObjectEntry." + arg0);
			if(null != m_method){
				if("params".equals(arg0)){
					m_handler.startElement(m_method, false, null);
					m_state = State.Arguments;
				}
				else if (State.Arguments.equals(m_state)){
					if("type".equals(arg0) && Element.Type.Object.equals(m_stk.peek().m_type)){
						m_bSetType = true;
					}
					else{
						if("value".equals(arg0) && m_handler.getTopObject().getType() instanceof ScalarDef){
							m_bSetScalarValue = true;
							ApiAlgs.getLog(this).trace(">>value");
						}
						else
							m_handler.startElement(arg0, false, null);
					}
				}
			}
			m_entry = arg0;
			startStackElement(new Element(Element.Type.Entry, m_entry));
			return true;
		}
		public boolean endObjectEntry() throws ParseException, IOException {
			Element tag = endStackElement(Element.Type.Entry);
			ApiAlgs.assertTrue(Element.Type.Entry.equals(tag.m_type), tag.toString());
			if(tag.equals(METHOD))
				m_method = (String) m_value;
			
			if(State.Arguments.equals(m_state)){
				if(m_bSetType){
					CurrentObject top = m_handler.getTopObject();
					String typeName = (String) m_value;
					top.setType(m_handler.getType(typeName, SCALARS.contains(typeName)));
					m_bSetType = false;
				}
				else{
					if(m_bSetScalarValue){
						ApiAlgs.getLog(this).trace("<<value:"+ m_value);
						m_bSetScalarValue = false;
//						String vStr = m_value.toString();
//						m_handler.characters(vStr.toCharArray(), 0, vStr.length());
					}
					else{
						m_handler.endElement();
						if(tag.equals(PARAMS)){
							m_state = State.Finit;
						}
					}
				}
			}
			
			return true;
		}	
		
		static final Element PARAMS = new Element(Element.Type.Entry, "params");
		static final Element PARAMS_ARRAY = new Element(Element.Type.Array, "arg");
		static final Element METHOD = new Element(Element.Type.Entry, "method");
		static final Element SCALAR_VALUE = new Element(Element.Type.Entry, "value");
		
		public boolean startArray() throws ParseException, IOException {
			Element top = m_stk.peek();
			String arrName = "value";
			if(top.equals(PARAMS))
				arrName = "arg";
			
			startStackElement(new Element(Element.Type.Array,arrName));
			return true;
		}
		
		public boolean endArray() throws ParseException, IOException {
			endStackElement(Element.Type.Array);
			return true;
		}

		private void startStackElement(Element element) {
			if(State.Arguments.equals(m_state)){
				if(!m_stk.isEmpty()){
					Element top = m_stk.peek();
					if(Element.Type.Array.equals(top.m_type)){
						String argName = top.m_name;
						if(top.m_name.equals("arg")){
							argName = "arg"+m_argIdx;
							m_argIdx++;
		
						}
						m_handler.startElement(argName, false, null);
					}
				}
			}
			if(null != element)
				m_stk.push(element);
			ApiAlgs.getLog(this).trace(">>" + element);
			
		}
		
		private Element endStackElement(Type type){
			Element res = null;
			if(null != type){
				res = m_stk.pop();
				ApiAlgs.assertEquals(type, res.m_type);
			}
			
			if(!m_stk.isEmpty() && Element.Type.Array.equals(m_stk.peek().m_type)){
				m_handler.endElement();
			}
			
			ApiAlgs.getLog(this).trace("<<" + res);
			return res;
		}
		
		public boolean primitive(Object arg0) throws ParseException,
				IOException {
			ApiAlgs.getLog(this).trace("primitive:" + arg0);
			Element top = m_stk.peek();
			if(Element.Type.Array.equals(top.m_type)){
				String argName = top.m_name;
				if(top.m_name.equals("arg")){
					argName = "arg"+m_argIdx;
					m_argIdx++;

				}
				m_handler.startElement(argName, false, null);
				String str = arg0.toString();
				m_handler.characters(str.toCharArray(), 0, str.length());
				m_handler.endElement();
			}
			else if(Element.Type.Entry.equals(top.m_type)){
				if(!m_bSetType){
					String str = arg0.toString();
					m_handler.characters(str.toCharArray(), 0, str.length());
				}
			}
			m_value = arg0;
			return true;
		}
	}

	public SOAPDocument deserialize(InterfaceDescription desc, InputStream source) throws IOException, ParseException{
//		BufferedReader reader = new BufferedReader(new InputStreamReader(source));
//		String str = reader.readLine();
//		Map<String, Object> map = (Map<String, Object>) js.deserialize(str);
//		SOAPDocument res = new SOAPDocument();
//		res.m_method = (String) map.get("method");
//		List params = (List) map.get("params");
		// res.m_args = params.toArray();
		// return res;
		SAXHandler handler = new SAXHandler(desc);

		JSONParser parser = new JSONParser();
		KeyFinder finder = new KeyFinder(handler);
		// finder.setMatchKey("id");
		InputStreamReader reader = new InputStreamReader(source);
		parser.parse(reader, finder, true);
		SOAPDocument res = new SOAPDocument();
		res.m_method = handler.m_method;
		res.m_args = handler.m_args;
		res.m_bIn = handler.m_bIn;
		return res;
	}

	public void serializeError(InterfaceDescription desc, Throwable t, OutputStream out) throws IOException {
		JsonRpc obj = new JsonRpcError("2.0",1, new Error(0, null));
		serializeObject(obj, out);
	}
}