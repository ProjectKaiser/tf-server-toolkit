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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.triniforce.soap.InterfaceDescription.MessageDef;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.InterfaceDescriptionGenerator.JsonResult;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.JSONSerializer.KeyFinder.Element.Type;
import com.triniforce.soap.SAXHandler.CurrentObject;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.EnumDef;
import com.triniforce.soap.TypeDef.MapDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.MapDefLib.MapComponentDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.utils.ApiAlgs;

import net.sf.sojo.core.UniqueIdGenerator;
import net.sf.sojo.core.filter.ClassPropertyFilter;
import net.sf.sojo.core.filter.ClassPropertyFilterHandlerImpl;
import net.sf.sojo.interchange.json.JsonSerializer;

public class JSONSerializer {
	public static class JsonRpc extends LinkedHashMap<String, Object>{
		private static final long serialVersionUID = -473423311133694151L;
		private String m_jsonrpc;
		Integer m_id;
		
		public JsonRpc(String jsonrpc, Integer id) {
			setJsonrpc(jsonrpc);
			setId(id);
		}
		public String getJsonrpc() {
			return m_jsonrpc;
		}
		public void setJsonrpc(String jsonrpc) {
			m_jsonrpc = jsonrpc;
			put("jsonrpc",jsonrpc);
		}
		public Integer getId() {
			return m_id;
		}
		public void setId(Integer id) {
			m_id = id;
			put("id",id);
		}
	}
	
	public static class JsonRpcMessage extends JsonRpc{
		private static final long serialVersionUID = 2086146444010066388L;
		String m_method;
		Object[] m_params;
		public JsonRpcMessage(String jsonrpc, String method, Object[] params,
				Integer id) {
			super(jsonrpc, id);
			setMethod(method);
			setParams(params);
			remove("id");
			setId(id);
		}
		public String getMethod() {
			return m_method;
		}
		public void setMethod(String method) {
			m_method = method;
			put("method", method);
		}
		public Object[] getParams() {
			return m_params;
		}
		public void setParams(Object[] params) {
			m_params = params;
			put("params", params);
		}
	}
	
	public static class JsonRpcError extends JsonRpc{
		private static final long serialVersionUID = -2474959020925509512L;
		public static class Error{
			private int m_code;
			private String m_message;
			private String m_stackTrace;
			
			public Error(){}
			public Error(int code, String msg, String stackTrace) {
				setCode(code);
				setMessage(msg);
				setStackTrace(stackTrace);
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

			public String getStackTrace() {
				return m_stackTrace;
			}

			public void setStackTrace(String stackTrace) {
				m_stackTrace = stackTrace;
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
			put("error", error);
		}
		
		
	}

	private JsonSerializer js;
	private Map<String, Boolean> m_configuration;
	
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
		Operation op = desc.getOperation(soap.m_method);
		MessageDef mdef = soap.m_bIn ? op.getRequestType() : op.getResponseType();
		List<PropDef> props = mdef.getProps();
		if(props.size() != soap.m_args.length){
			throw new ESoap.EWrongArgumentNumber(soap.m_method);
		}
		Object[] mobj = new Object[soap.m_args.length];
		int i =0 ;
		for(PropDef pd : props){
			Object value;
			value = toJsonObject(desc, pd.getType(), soap.m_args[i]);
			mobj[i] = value;
			i++;
		}
		JsonRpc obj;
		if(soap.m_bIn)
			obj = new JsonRpcMessage("2.0", soap.m_method, mobj, 1);
		else{
			JsonResult jsonRes = new JsonResult();
			if(soap.m_args.length > 0)
				jsonRes.setResult(mobj[0]);
			obj = jsonRes;
		}
		serializeObject(obj, out);
	}
	
	private Object toJsonObject(InterfaceDescription desc, TypeDef type, Object object) {
		Object value;
		if(type instanceof EnumDef){
			ScalarDef sd = (ScalarDef) type;
			value = sd.stringValue(object, desc, true, TypeDef.ContType.JSON);				
		}
		else if(type.getType().equals(java.util.Date.class.getName())){
			value = ((ScalarDef)type).stringValue(object, desc, true, TypeDef.ContType.JSON);
		}
		else if(type instanceof ClassDef){
			ClassDef cd = (ClassDef) type;
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			
			 Class objCls = object.getClass();
             
             if(!objCls.getName().equals(cd.getType())){
                 TypeDef hideTypeDef = desc.getType(objCls);
                 if(null != hideTypeDef){
                	 if(!(hideTypeDef instanceof ClassDef))
                		 throw new RuntimeException("wrong argument type : " + objCls.getName());
                	 
                     String typeName = hideTypeDef.getName();
                     map.put("type", typeName);
                     cd = (ClassDef) hideTypeDef;
                 }
             }
			for(PropDef pd : cd.getProps()){
				map.put(pd.getName(), toJsonObject(desc, pd.getType(), pd.get(object)));
			}
			value = map;
		}
		else if (type instanceof ArrayDef){
            ArrayDef ad = (ArrayDef) type;
            PropDef propDef = ad.getPropDef();
            Collection col = (Collection) propDef.get(object);
            ArrayList<Object> l = new ArrayList<Object>(col.size());
            for (Object o1 : col) {
                l.add(toJsonObject(desc, propDef.getType(), o1));
            }
            value = l;
			
		}
		else
			value = object;
		return value;
	}

	public void serializeObject(Object obj, OutputStream out) throws IOException {
		Object res = js.serialize(obj, new String[]{UniqueIdGenerator.UNIQUE_ID_PROPERTY, "class"});
		String str = res.toString();
		out.write(str.getBytes("utf-8"));
	}
	
	static class KeyFinder implements ContentHandler{
		
		static final Element PARAMS = new Element(Element.Type.Entry, "params");
		static final Element PARAMS_ARRAY = new Element(Element.Type.Array, "arg");
		static final Element METHOD = new Element(Element.Type.Entry, "method");
		static final Element SCALAR_VALUE = new Element(Element.Type.Entry, "value");
		static final Element RESULT = new Element(Element.Type.Entry, "result");

		enum State {Method, Arguments, Finit};
		
		private SAXHandler m_handler;
		private String m_method = null;
		private Object m_value;
		
		private String m_entry;
		int m_argIdx = 0;
		
		private State m_state;
		private boolean m_bSetType=false;
		private boolean m_bSetScalarValue=false;
		
		static final HashMap<String, String> SCALARS = new HashMap<String, String>();
		static {
			SCALARS.put("Boolean", "boolean");
			SCALARS.put("Int", "int");
			SCALARS.put("Long", "long");
			SCALARS.put("Short", "short");
			SCALARS.put("Float", "float");
			SCALARS.put("Double", "double");
			SCALARS.put("String", "string"); 
			SCALARS.put("Object", "object");
			SCALARS.put("DateTime", "dateTime");
			SCALARS.put("Decimal", "decimal");
			SCALARS.put("Base64Binary", "base64Binary");
		}
		
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
		private List<String> m_argNames;
		private InterfaceDescription m_desc;

		public KeyFinder(SAXHandler handler, InterfaceDescription desc) {
			m_handler = handler;
			m_argNames = null;
			m_desc = desc;
		}



		@Override
		public void startJSON() throws ParseException, IOException {
		}
		@Override
		public void endJSON() throws ParseException, IOException {;

		}

		@Override
		public boolean startObject() throws ParseException, IOException {

			startStackElement(new Element(Element.Type.Object, ""));
			m_bSetType=false;
			return true;
		}
		@Override
		public boolean endObject() throws ParseException, IOException {

			endStackElement(Element.Type.Object);
			return true;
		}
		
		@Override
		public boolean startObjectEntry(String arg0) throws ParseException,
				IOException {
			if(null != m_method){
				if(PARAMS.m_name.equals(arg0)){
					m_handler.startElement(m_method, false, null);
					m_state = State.Arguments;
					m_argNames = getArgumentNames(m_desc, m_method);
				}
				else if (State.Arguments.equals(m_state)){
					if("type".equals(arg0) && Element.Type.Object.equals(m_stk.peek().m_type)){
						m_bSetType = true;
					}
					else{
						if("value".equals(arg0) && m_handler.getTopObject().getType() instanceof ScalarDef){
							m_bSetScalarValue = true;
						}
						else if (m_handler.getTopObject().getType() instanceof MapDef){
								m_handler.startElement("value", false, null);
								m_handler.startElement("key", false, null);
								m_handler.characters(arg0.toCharArray(), 0, arg0.length());
								m_handler.endElement();
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
		private List<String> getArgumentNames(InterfaceDescription desc, String opName) {
			Operation op = desc.getOperation(opName);
			MessageDef mdef = (MessageDef) op.getProp(opName).getType();
			List<PropDef> props = mdef.getProps();
			ArrayList<String> res = new ArrayList<String>(props.size());
			for (PropDef prop : props) {
				res.add(prop.getName());
			}
			return res;
		}



		@Override
		public boolean endObjectEntry() throws ParseException, IOException {

			Element tag = endStackElement(Element.Type.Entry);
			ApiAlgs.assertTrue(Element.Type.Entry.equals(tag.m_type), tag.toString());
			if(tag.equals(METHOD))
				m_method = (String) m_value;
			
			if(State.Arguments.equals(m_state)){
				if(m_bSetType){
					CurrentObject top = m_handler.getTopObject();
					String typeName = SCALARS.get(m_value);
					boolean bScalar = null != typeName;
					if(!bScalar)
						typeName = (String) m_value;
					TypeDef type = m_handler.getType(typeName, bScalar);
					
					top.setType(type);
					m_bSetType = false;
				}				
				else{
					if(m_bSetScalarValue){
						m_bSetScalarValue = false;
//						String vStr = m_value.toString();
//						m_handler.characters(vStr.toCharArray(), 0, vStr.length());
					}
					else if (m_handler.getTopObject().getType() instanceof MapDef){
						m_handler.endElement();//Map.value
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
		
		
		@Override
		public boolean startArray() throws ParseException, IOException {
			Element top = m_stk.peek();
			String arrName = "value";
			if(top.equals(PARAMS))
				arrName = "arg";
			
			startStackElement(new Element(Element.Type.Array,arrName));
			return true;
		}
		
		@Override
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
							if(m_argIdx >= m_argNames.size())
								throw new ESoap.EUnknownElement("arg" + m_argIdx);
							argName = m_argNames.get(m_argIdx);
							m_argIdx++;
		
						}
						m_handler.startElement(argName, false, null);
					}
				}
			}
			if(null != element)
				m_stk.push(element);
			
		}
		
		private Element endStackElement(Type type){
			Element res = null;
			if(null != type){
				res = m_stk.pop();
				ApiAlgs.assertEquals(type, res.m_type);
			}
			
			if(State.Arguments.equals(m_state)){
				if(!m_stk.isEmpty() && Element.Type.Array.equals(m_stk.peek().m_type)){
					m_handler.endElement();
				}
			}
			
			return res;
		}
		
		@Override
		public boolean primitive(Object arg0) throws ParseException,
				IOException {
			Element top = m_stk.peek();
			if(State.Arguments.equals(m_state)){
				if(Element.Type.Array.equals(top.m_type)){
					String argName = top.m_name;
					if(top.m_name.equals("arg")){
						if(m_argIdx >= m_argNames.size())
							throw new ESoap.EUnknownElement("arg" + m_argIdx);
						argName = m_argNames.get(m_argIdx);
						m_argIdx++;
	
					}
					boolean bNull = null == arg0;
					m_handler.startElement(argName, bNull, null);
					setPrimitive(arg0);
					m_handler.endElement();
				}
				else if (m_handler.getTopObject().getType() instanceof MapComponentDef){
					
					m_handler.startElement("value", false, null);
					setPrimitive(arg0);
					m_handler.endElement();
				}
				else if(Element.Type.Entry.equals(top.m_type)){
					if(!m_bSetType){
						setPrimitive(arg0);
					}
				}
			}
			m_value = arg0;
			return true;
		}



		private void setPrimitive(Object arg0) {
			if(null == arg0)
				return ;
			CurrentObject co = m_handler.getTopObject();
			TypeDef td = co.getType();
			if(td.getName().equals("object")){
				if(arg0 instanceof Number){
					co.setType(m_handler.getType(numericType(arg0.getClass()), true));
				}
			}
			String str = arg0.toString();
			m_handler.characters(str.toCharArray(), 0, str.length());
		}



		private String numericType(Class<? extends Object> cls) {
			return ScalarDef.scalarName(cls.getName());
		}
	}

	public SOAPDocument deserialize(InterfaceDescription desc, InputStream source) throws IOException, ParseException{
		SAXHandler handler = new SAXHandler(desc, m_configuration);

		JSONParser parser = new JSONParser();
		KeyFinder finder = new KeyFinder(handler, desc);
		InputStreamReader reader = new InputStreamReader(source, Charset.forName("UTF-8"));
		parser.parse(reader, finder, true);
		SOAPDocument res = new SOAPDocument();
		res.m_method = handler.m_method;
		res.m_args = handler.m_args;
		res.m_bIn = handler.m_bIn;
		return res;
	}

}
