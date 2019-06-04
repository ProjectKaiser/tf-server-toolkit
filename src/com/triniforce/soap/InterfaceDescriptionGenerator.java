/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.triniforce.soap.ESoap.EParameterizedException;
import com.triniforce.soap.InterfaceDescription.MessageDef;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.InterfaceDescriptionGenerator.ObjectConverter.TypedObject;
import com.triniforce.soap.InterfaceOperationDescription.NamedArg;
import com.triniforce.soap.JSONSerializer.JsonRpc;
import com.triniforce.soap.JSONSerializer.JsonRpcError;
import com.triniforce.soap.JSONSerializer.JsonRpcError.Error;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.WsdlDescription.WsdlMessage;
import com.triniforce.soap.WsdlDescription.WsdlPort.WsdlOperation;
import com.triniforce.soap.WsdlDescription.WsdlType;
import com.triniforce.soap.WsdlDescription.WsdlType.Restriction;
import com.triniforce.soap.WsdlDescription.WsdlTypeElement;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiAlgs.RethrownException;
import com.triniforce.utils.EUtils;
import com.triniforce.utils.IName;
import com.triniforce.utils.TFUtils;

import net.sf.sojo.interchange.json.JsonParserException;
import net.sf.sojo.interchange.json.JsonSerializer;

public class InterfaceDescriptionGenerator {
    
    private String m_targetNamespace;
    private String m_serviceName;
    
    private SAXParserFactory m_SAXParserFactory;
    private DocumentBuilderFactory m_documentBuilderFactory;
    private TransformerFactory m_transformerFactory;
    List<CustomSerializer<?,?>> m_customSerializers = new ArrayList<CustomSerializer<?,?>>();
	private Map<String, Boolean> m_configuration = new HashMap<String, Boolean>();

    public InterfaceDescriptionGenerator() {
        this("http://tempuri.org/", "ServerName");
    }
    
    public InterfaceDescriptionGenerator(String targetNamespace, String serviceName) {
        m_targetNamespace = targetNamespace;
        m_serviceName = serviceName;
                
        m_SAXParserFactory = SAXParserFactory.newInstance();
        m_SAXParserFactory.setNamespaceAware(true);
        m_documentBuilderFactory = DocumentBuilderFactory.newInstance();
        m_documentBuilderFactory.setNamespaceAware(true);
        m_transformerFactory = TransformerFactory.newInstance();
    }
    
    
    /**
     * Create new InterfaceDescription by class and previous description
     * in new description added all types contained by  TypeLibrary
     * @param oldDesc - previous description
     * @param cls - class to parse
     * @return - new description
     */
    public InterfaceDescription parse(InterfaceDescription oldDesc, Class<?> cls){
    	return parse(oldDesc, cls, cls.getPackage());
    }
    
    /**
     * Create new InterfaceDescription by class and previous description
     * in new description added all types contained by  TypeLibrary
     * @param oldDesc - previous description
     * @param cls - class to parse
     * @return - new description
     */
    public InterfaceDescription parse(InterfaceDescription oldDesc, Class<?> cls, Package pkg){
        try{
        	SoapInclude anno = cls.getAnnotation(SoapInclude.class);
        	List<SoapInclude> incs = null == anno ? Collections.EMPTY_LIST : Arrays.asList(anno);
	    	return parse(oldDesc, listInterfaceOperations(cls, false), pkg, incs);
        } catch (IntrospectionException e) {
            ApiAlgs.rethrowException(e);
            return null;
        }
    }
    
    public List<InterfaceOperationDescription> listInterfaceOperations(Class<?> cls, boolean bMultiClass) throws IntrospectionException {
    	String pkgPrefix = null;
    	if(bMultiClass){
    		pkgPrefix = classPrefix(cls);
    	}
    	
    	BeanInfo info = Introspector.getBeanInfo(cls);
        List<InterfaceOperationDescription> methods = new ArrayList<InterfaceOperationDescription>();
        for (MethodDescriptor mDesc : info.getMethodDescriptors()) {
        	if(!mDesc.getMethod().getDeclaringClass().getPackage().equals(cls.getPackage()))
        		continue;
        	InterfaceOperationDescription opDesc = new InterfaceOperationDescription();
        	opDesc.setName(mDesc.getName());
        	ArrayList<NamedArg> args = new ArrayList<NamedArg>();
        	int i = 0;
            for(Type type : mDesc.getMethod().getGenericParameterTypes()){
            	args.add(new NamedArg("arg"+i++, type));
            }
        	opDesc.setArgs(args);
        	opDesc.setResult(new NamedArg(mDesc.getName()+"Result", mDesc.getMethod().getGenericReturnType()));
        	opDesc.setPkgPrefix(pkgPrefix);
        	opDesc.getThrows().addAll(Arrays.asList(mDesc.getMethod().getGenericExceptionTypes()));
        	methods.add(opDesc);
        }
        return methods;
	}

	public String classPrefix(Class<?> cls) {
		String pkgName = cls.getPackage().getName();
		int idx = pkgName.lastIndexOf('.');
		return idx < 0 ? pkgName : pkgName.substring(idx + 1);
	}

//    private Operation parseOperation(String mName, Method method, TypeDefLibCache lib) {
//        MessageDef inMsgType = new InterfaceDescription.MessageDef(mName);
//        int i=0;
//        for(Type type : method.getGenericParameterTypes()){
//            TypeDef def = lib.add(type);
//            inMsgType.addParameter("arg"+i++, TypeDefLibCache.toClass(type), def);
//        }
//        
//        MessageDef outMsgType = new MessageDef(mName + "Response");
//        Type retType = method.getGenericReturnType();
//        if(!retType.equals(Void.TYPE)){
//            outMsgType.addParameter(mName+"Result", TypeDefLibCache.toClass(retType), lib.add(retType));
//        }
//        
//        return new Operation(mName, inMsgType, outMsgType);
//    }
    
    private Operation parseOperation(InterfaceOperationDescription opDesc, TypeDefLibCache lib) {
        MessageDef inMsgType = new InterfaceDescription.MessageDef(opDesc.getName());
        for(NamedArg arg: opDesc.getArgs()){
        	Type argType = arg.getType();
            CustomSerializer<?,?> customSrz = CustomSerializer.find(m_customSerializers, 
            		TypeDefLibCache.toClass(argType));
            if(null != customSrz){
                TypeDef def = lib.add(customSrz.getTargetType());            
            	inMsgType.addParameter(arg.getName(), TypeDefLibCache.toClass(argType), def, customSrz);
            }
            else{
                TypeDef def = lib.add(argType);            
            	inMsgType.addParameter(arg.getName(), TypeDefLibCache.toClass(arg.getType()), def);
            }
        }
        
        MessageDef outMsgType = new MessageDef(opDesc.getName() + "Response");
        Type retType = opDesc.getResult().getType();
        if(!retType.equals(Void.TYPE)){
            outMsgType.addParameter(opDesc.getResult().getName(), TypeDefLibCache.toClass(retType), lib.add(retType));
        }
        
        String opName = null == opDesc.getPkgPrefix() ? opDesc.getName() : String.format("%s_%s", opDesc.getPkgPrefix(), opDesc.getName());
        
        ArrayList<ClassDef> vthrows = new ArrayList<ClassDef>();
        for(Type typ : opDesc.getThrows()){        	
        	if(typ instanceof Class){
        		Class cls = (Class) typ;
        		if((EParameterizedException.class.isAssignableFrom(cls) && !cls.equals(EParameterizedException.class)) ||
        				null != lib.get(typ)){
        			vthrows.add((ClassDef) lib.add(typ));
        		}
        	}
        }
        
        return new Operation(opName, inMsgType, outMsgType, vthrows);
    }

    static String wsdl = "http://schemas.xmlsoap.org/wsdl/";
    static String schema = "http://www.w3.org/2001/XMLSchema";
    static String soap = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static String soapenv = "http://schemas.xmlsoap.org/soap/envelope/";
    public static String soapenv12 = "http://www.w3.org/2003/05/soap-envelope";
    static String xsi = "http://www.w3.org/2001/XMLSchema-instance";
    static String xmlns = "http://www.w3.org/2000/xmlns/";
    
    interface IConverter<T>{
        void run(Node_S parent, T val);
    }
    
    static class Node_S{
        private Node_S m_parent;
        private Element m_element;
        public Node_S(Element element, Node_S parent) {
            m_element = element;
            m_parent = parent;
        }
        
        Node_S append(String name){
            Element e = getDocument().createElement(name);
            m_element.appendChild(e);
            Node_S res = new Node_S(e, this);
            return res;
        }
        
        <T> Node_S append (Collection<T> col, IConverter<T> conv){
            for (T v: col) {
                conv.run(this, v);
            }
            return this;    
        }
        <T> Node_S append (T val, IConverter<T> conv){
            conv.run(this, val);
            return this;    
        }
        
        Node_S attr(String name, String v){
            m_element.setAttribute(name, v);
            return this;
        }
        Node_S end(){
            return m_parent;
        }

        public Node_S text(String string) {
            m_element.setTextContent(string);
            return this;
        }
        Document getDocument(){
            return m_element.getOwnerDocument();
        }
    }
    
    static class TypeConverter implements IConverter<WsdlType>{
        private boolean m_bShowName;
		private String m_tns;
        public TypeConverter(boolean bShowName, String tns) {
            m_bShowName = bShowName;   
            m_tns = tns;
        }
        @Override
		public void run(Node_S parent, WsdlType val) {
            Node_S t = parent.append(val.isComplex() ? "s:complexType" : "s:simpleType");
            if(m_bShowName)
                t.attr("name", val.getTypeDef().getName());
            
            String extBase = val.getParentName();
            if(null != extBase){
                t = t.append("s:complexContent")
                    .attr("mixed", "false")
                    .append("s:extension")
                        .attr("base", "tns:"+extBase);
            }
            Collection<WsdlTypeElement> elements = val.getElements();
            if(!elements.isEmpty()){
	            t.append("s:sequence")
	                .append(elements, new IConverter<WsdlTypeElement>(){
	                    @Override
						public void run(Node_S parent, WsdlTypeElement val) {
	                        int maxOccur = val.getMaxOccur();
	                        String sMaxOccur = maxOccur == -1 ? "unbounded" : Integer.toString(maxOccur);
	                        Node_S e = parent.append("s:element")
	                            .attr("minOccurs", Integer.toString(val.getMinOccur()))
	                            .attr("maxOccurs", sMaxOccur)
	                            .attr("name", val.getName());
	                        if(!val.isResidentType()){
	                            e.append(val.getType(), new TypeConverter(false, m_tns));
	                        }
	                        else{
	                            if(!val.getType().getTypeDef().getType().equals(Object.class.getName()))
	                                e.attr("type", getTypeName(val.getType()));                            
	                        }
	                        if(val.getMinOccur()>0 && val.isNillable())
	                        	e.attr("nillable", "true");
	                    }
	                })
	                .end()
	            .end();
            }
            Restriction restrBase = val.getResriction();
            if(null != restrBase){
            	WsdlType restrType = new WsdlType(restrBase.m_base);
            	Node_S e = t.append("s:restriction").attr("base", getTypeName(restrType));
            	if(null != restrBase.m_vals)
	            	for (String value : restrBase.m_vals) {
	            		e.append("s:enumeration").attr("value", value);
					}	
            }
        }
        public String getTypeName(WsdlType type) {
        	String ns = type.getNamespace(m_tns);
            String res = type.getTypeDef().getName();
            if(InterfaceDescriptionGenerator.schema.equals(ns)){
                res = "s:"+ res;
            }
            else{
                res = "tns:"+res;
            }
            return res;
        }
    }

    @SuppressWarnings("unchecked")
    public Document generateWSDL(WsdlDescription desc, String location) {
        Document doc = null;
        try {
            DocumentBuilder db = getDocumentBuilder();
            doc = db.newDocument();
            
            Element eDefs = doc.createElement("wsdl:definitions");
            doc.appendChild(eDefs);
            final TypeConverter typeConverter = new TypeConverter(true, m_targetNamespace);
            TFUtils.assertEquals(null,
            new Node_S(eDefs, null)
                .attr("xmlns:soap", soap)
                .attr("xmlns:soapenc", "http://schemas.xmlsoap.org/soap/encoding/")
                .attr("xmlns:mime", "http://schemas.xmlsoap.org/wsdl/mime/")
                .attr("xmlns:tns", m_targetNamespace)
                .attr("xmlns:s", "http://www.w3.org/2001/XMLSchema")
                .attr("xmlns:soap12", "http://schemas.xmlsoap.org/wsdl/soap12/")
                .attr("xmlns:http", "http://schemas.xmlsoap.org/wsdl/http/")
                .attr("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/")
                .attr("targetNamespace", m_targetNamespace)
                .append("wsdl:types")
                    .append("s:schema")
                    	.attr("elementFormDefault", "qualified")
                    	.attr("targetNamespace", m_targetNamespace)
                        .append(desc.getWsdlTypeElements(), new IConverter<WsdlTypeElement>(){
                            @Override
							public void run(Node_S parent, WsdlTypeElement val) {
                                TypeConverter tc = new TypeConverter(false, m_targetNamespace);
                                parent.append("s:element")
                                    .attr("name", val.getName())
                                    .append(val.getType(), tc)
                                .end();
                            }
                        })
                        // RPC style for exception, Delphi doesn't understand document style
                        /*.append(desc.getWsdlFaults(), new IConverter<WsdlTypeElement>(){
							@Override
							public void run(Node_S parent, WsdlTypeElement val) {
                                parent.append("s:element")
	                                .attr("name", val.getName())
	                                .attr("type", typeConverter.getTypeName(val.getType()))
	                            .end();								
							}
                        })*/
                        .append(desc.getWsdlTypes(), typeConverter)
                    .end()
                .end()
                .append(desc.getMessages(), new IConverter<WsdlMessage>(){
                    @Override
					public void run(Node_S parent, WsdlMessage val) {
                        parent.append("wsdl:message")
                            .attr("name", val.getMessageName())
                            .append("wsdl:part")
                                .attr("name", "parameters")
                                .attr("element", "tns:"+val.getElementName())
                            .end()
                        .end();
                    }
                })
                .append(desc.getWsdlFaults(), new IConverter<WsdlTypeElement>(){
                    @Override
					public void run(Node_S parent, WsdlTypeElement val) {
                    	String typename = val.getType().getTypeDef().getName();
                        parent.append("wsdl:message")
                            .attr("name", typename)
                            .append("wsdl:part")
                                .attr("name", typename)
                                .attr("type", typeConverter.getTypeName(val.getType()))
                            .end()
                        .end();
                    }
                })
                .append("wsdl:portType")
                    .attr("name", m_serviceName + "Soap")
                    .append(desc.getWsdlPort().getOperations(), new IConverter<WsdlOperation>(){
                        @Override
						public void run(Node_S parent, WsdlOperation val) {
                            parent.append("wsdl:operation")
                                .attr("name", val.getName())
                                .append("wsdl:input")
                                    .attr("message", "tns:"+val.getInput().getMessageName())
                                .end()
                                .append("wsdl:output")
                                    .attr("message", "tns:"+val.getOutput().getMessageName())
                                .end()
                                .append(val.getFaults(),  new IConverter<WsdlType>(){
									@Override
									public void run(Node_S parent, WsdlType val) {
										parent.append("wsdl:fault")
											.attr("name", val.getTypeDef().getName())
											.attr("message", typeConverter.getTypeName(val));
									}
                                })
                            .end();
                        }
                    })
                .end()
                .append("wsdl:binding")
                    .attr("name", m_serviceName + "Soap")
                    .attr("type", "tns:"+m_serviceName+"Soap")
                    .append("soap:binding")
                        .attr("transport", "http://schemas.xmlsoap.org/soap/http")
                    .end()
                    .append(desc.getWsdlPort().getOperations(), new IConverter<WsdlOperation>(){
                        @Override
						public void run(Node_S parent, WsdlOperation val) {
                            parent.append("wsdl:operation")
                                .attr("name", val.getName())
                                .append("soap:operation")
                                    .attr("soapAction", m_targetNamespace + val.getName())
                                    .attr("style", "document")
                                .end()
                                .append("wsdl:input")
                                    .append("soap:body")
                                        .attr("use","literal")
                                    .end()
                                .end()
                                .append("wsdl:output")
                                    .append("soap:body")
                                        .attr("use","literal")
                                    .end()
                                .end()
                                .append(val.getFaults(), new IConverter<WsdlType>(){
									@Override
									public void run(Node_S parent, WsdlType val) {
										parent.append("wsdl:fault")
											.attr("name", val.getTypeDef().getName())
											.append("soap:fault")
												.attr("name", val.getTypeDef().getName())
												.attr("use","literal")
											.end()
										.end();
									}                                	
                                })
                            .end();
                        }
                    })
                .end()
                .append("wsdl:binding")
                    .attr("name", m_serviceName + "Soap12")
                    .attr("type", "tns:"+m_serviceName+"Soap")
                    .append("soap12:binding")
                        .attr("transport", "http://schemas.xmlsoap.org/soap/http")
                    .end()
                    .append(desc.getWsdlPort().getOperations(), new IConverter<WsdlOperation>(){
                        @Override
						public void run(Node_S parent, WsdlOperation val) {
                            parent.append("wsdl:operation")
                                .attr("name", val.getName())
                                .append("soap12:operation")
                                    .attr("soapAction", m_targetNamespace + val.getName())
                                    .attr("style", "document")
                                .end()
                                .append("wsdl:input")
                                    .append("soap12:body")
                                        .attr("use","literal")
                                    .end()
                                .end()
                                .append("wsdl:output")
                                    .append("soap12:body")
                                        .attr("use","literal")
                                    .end()
                                .end()
                            .end();
                        }
                    })
                .end()
                .append("wsdl:service")
                    .attr("name", m_serviceName)
                    .append("wsdl:port")
                        .attr("name", m_serviceName+"Soap")
                        .attr("binding", "tns:"+m_serviceName+"Soap")
                        .append("soap:address")
                            .attr("location", location)
                        .end()
                    .end()
                    .append("wsdl:port")
                        .attr("name", m_serviceName+"Soap12")
                        .attr("binding", "tns:"+m_serviceName+"Soap12")
                        .append("soap12:address")
                            .attr("location", location)
                        .end()
                    .end()
                .end()
            .end());
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return doc;
    }

    
    
    public static class SOAPDocument{
        public String   m_soap;
        public String   m_method;
        public Object[] m_args;
        public boolean  m_bIn;
    }

    public SOAPDocument deserialize(InterfaceDescription desc, InputStream source) throws ParserConfigurationException, SAXException, IOException {
    	return deserialize(desc, source, new IParser(){
			@Override
			public void parse(InputStream source, SoapHandler handler) throws SAXException, IOException, ParserConfigurationException {
		        SAXParser parser = getParser();
		        parser.parse(new InputSource(source), handler);
			}
    	});
    }
    
    public SOAPDocument deserializeJson(InterfaceDescription desc, InputStream source) throws IOException, ParserConfigurationException, ParseException{
    	JSONSerializer js = new JSONSerializer();
    	return js.deserialize(desc, source);
//		return deserialize(desc, source, new JsonParser(){
//			protected void parseHeader(Map<String, Object> src, SoapHandler handler) throws SAXException {
//				String methodName  = (String) src.get("method");
//				handler.startElement(m_targetNamespace, methodName, null, EMPTY_ATTRS);
//				List<Object> params = (List<Object>) src.get("params");
//				int i = 0;
//				for(Object value : params){
//					String argName = "arg"+i;
//					parseValue(handler, argName, value);
//					i++;
//				}
//				handler.endElement(m_targetNamespace, methodName, null);
//			}
//		});
    }
    

	public SOAPDocument deserializeJsonResponse(InterfaceDescription desc,
			final String method, InputStream source) throws SAXException, IOException, ParserConfigurationException {
		return deserialize(desc, source, new JsonParser(){
			@Override
			protected void parseHeader(Map<String, Object> src,
					SoapHandler handler) throws SAXException {
				String mHeader = method+"Response";
				handler.startElement(m_targetNamespace, mHeader, null, EMPTY_ATTRS);
				parseValue(handler, method+"Result", src.get("result"));
				handler.endElement(m_targetNamespace, mHeader, null);
			}
		});
	}
    
    interface IParser{
    	void parse(InputStream source, SoapHandler handler) throws SAXException, IOException, ParserConfigurationException;
    }
    
	static final AttributesImpl EMPTY_ATTRS = new AttributesImpl();
    class JsonParser implements IParser{
		
		@Override
		public void parse(InputStream source, SoapHandler handler) throws SAXException, IOException {
			callStartSoapHeaders(handler);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(source));
			String str = reader.readLine();
			JsonSerializer js = new JsonSerializer();
			Map<String, Object> src = (Map<String, Object>) js.deserialize(str);
			parseHeader(src, handler);
			
			callEndSoapHeaders(handler);
		}
		
		protected void parseHeader(Map<String, Object> src, SoapHandler handler) throws SAXException {
		}

		private void parseMap(SoapHandler handler, Map<String, Object> value) throws SAXException {
			for(Map.Entry<String, Object> entry: value.entrySet()){
				parseValue(handler, entry.getKey(), entry.getValue());
			}
		}

		protected void parseValue(SoapHandler handler, String name, Object value) throws SAXException {
			handler.startElement(m_targetNamespace, name, null, EMPTY_ATTRS);
			if(value instanceof Map){
				parseMap(handler, (Map<String, Object>) value);
			}
			else if(value instanceof List){
				parseList(handler, (List<Object>) value);
			}
			else{
				String argValue = value.toString(); 
				handler.characters(argValue.toCharArray(), 0, argValue.length());
			}
			handler.endElement(m_targetNamespace, name, null);			
		}

		private void parseList(SoapHandler handler, List<Object> value) throws SAXException {
			for (Object object : value) {
				parseValue(handler, "value", object);
			}
		}

		private void callStartSoapHeaders(SoapHandler handler) throws SAXException {
			handler.startElement(soapenv, "Envelope", null, null);
			handler.startElement(soapenv, "Body", null, null);
			
		}
		
		private void callEndSoapHeaders(SoapHandler handler) throws SAXException {
			handler.endElement(soapenv, "Body", null);
			handler.endElement(soapenv, "Envelope", null);
			
		}

    }
    
    private SOAPDocument deserialize(InterfaceDescription desc, InputStream source, IParser parser) throws SAXException, IOException, ParserConfigurationException{
        SoapHandler h = new SoapHandler(m_targetNamespace, desc, m_configuration );
        parser.parse(source, h);
        if(null != h.m_fault)
        	throw new ESoap.EFaultCode(h.m_fault.m_string);
        SOAPDocument res = h.getResult();
        return res;
    }
    
    private SAXParser getParser() throws ParserConfigurationException, SAXException {
        return m_SAXParserFactory.newSAXParser();
    }
    
    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return m_documentBuilderFactory.newDocumentBuilder();
    }
    
    private Transformer getTransformer() throws TransformerConfigurationException{
        Transformer t = m_transformerFactory.newTransformer();
        t.setOutputProperty("indent", "yes");
        
        //t.setOutputProperty("encoding","utf-8");
        return t;
    }

    static <T extends IName> int getPos(List<T> col, String tag) {
        for (int i=0; i<col.size(); i++) {
            if(col.get(i).getName().equals(tag))
                return i;
        }
        return -1;
    }
    
    static <T extends IName> T findByName(List<T> col, String tag) {
        int iPos = getPos(col, tag);
        return iPos==-1 ? null : col.get(iPos);
    }
    
    static class ObjectConverter implements IConverter<ObjectConverter.TypedObject>{
        
        private InterfaceDescription m_lib;
        private boolean bSetNS;
        private String m_targetNS;

        ObjectConverter(InterfaceDescription desc, String targetNS){
            m_lib = desc;
            bSetNS = true;
            m_targetNS = targetNS;
        }
        
        static class  TypedObject{
            private Object m_obj;
            private PropDef m_propDef;
            public TypedObject(PropDef propDef, Object obj) {
                m_propDef = propDef;
                m_obj = obj;
            }
            String  getPropName() {
                return m_propDef.getName();
            }
            TypeDef getTypeDef() {
                return m_propDef.getType();
            }
            Object  getObject() {
                return m_obj;
            }
        }
        
        static class TypedProps extends AbstractList<TypedObject>{

            private List<PropDef> m_props;
            private Object m_obj;

            public TypedProps(List<PropDef> props, TypedObject val) {
                m_props = props;
                m_obj = val.getObject();
            }

            @Override
            public TypedObject get(int arg0) {
                PropDef propDef = m_props.get(arg0);
                Object res = propDef.get(m_obj);
                return new TypedObject(propDef, res);
            }

            @Override
            public int size() {
                return m_props.size();
            }
            
        }
        
        @Override
		@SuppressWarnings("unchecked")
        public void run(Node_S parent, TypedObject val) {
            Node_S e = parent.append(val.getPropName());
            if(bSetNS){
                e.attr("xmlns", m_targetNS);
                bSetNS = false;
            }
            if(null == val.getObject()){
                e.attr("xsi:nil", "true");
            }
            else{
                TypeDef typeDef = val.getTypeDef();
                if(typeDef instanceof ClassDef || Object.class.getName().equals(typeDef.getType())){
                    Class objCls = val.getObject().getClass();
                    
                    if(!objCls.getName().equals(typeDef.getType())){
                        TypeDef hideTypeDef = m_lib.getType(val.getObject().getClass());
                        if(null != hideTypeDef){
                            String typeName;
                            if(hideTypeDef instanceof ScalarDef)
                                typeName = "xsd:"+hideTypeDef.getName();
                            else
                                typeName = hideTypeDef.getName();
                            e.attr("xsi:type", typeName);
                            typeDef = hideTypeDef;
                        }
                    }
                }
                
                if(typeDef instanceof ClassDef){
                    ClassDef cd = (ClassDef) typeDef;
                    e.append(new TypedProps(cd.getProps(), val), this);
                }
                else if(typeDef instanceof ArrayDef){
                    ArrayDef ad = (ArrayDef) typeDef;
                    PropDef propDef = ad.getPropDef();
                    Collection col = (Collection) propDef.get(val.getObject());
                    for (Object object : col) {
                        e.append(new TypedObject(propDef, object), this);
                    }
                }
                else if(typeDef instanceof ScalarDef){
                	String str = ((ScalarDef) typeDef).stringValue(val.getObject());
                    e.text(str);
                }
            }
            e.end();
        }
    } 
    

    public Document serialize(InterfaceDescription desc, final SOAPDocument soap) {
        Document doc = null;
        try {
            Operation op = findByName(desc.getOperations(), soap.m_method);
            if(null == op)
            	throw new NoSuchElementException(soap.m_method);
            PropDef prop = soap.m_bIn ? op.getProps().get(0) : op.getProps().get(1);
            
            Node_S body = createSoapDocument(soap.m_soap);
            body.append(new TypedObject(prop, soap.m_args), new ObjectConverter(desc, m_targetNamespace));
            doc = body.getDocument();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return doc;
    }

    private Node_S createSoapDocument(String soapNS) throws ParserConfigurationException {
        DocumentBuilder db = getDocumentBuilder();
        Document doc = db.newDocument();
        
        Element eDefs = doc.createElement("soap:Envelope");
        doc.appendChild(eDefs);
        return new Node_S(eDefs, null)
            .attr("xmlns:soap", soapNS)
            .attr("xmlns:xsd", "http://www.w3.org/2001/XMLSchema")
            .attr("xmlns:xsi", xsi)
            .append("soap:Body");
        
    }

    public Document serializeException(String soapNS, Throwable throwable, InterfaceDescription desc, String method) {
        Document doc = null;
        try {
        	if(null == soapNS)
        		soapNS = soapenv;
            Node_S body = createSoapDocument(soapNS);
            Node_S fault = body.append("soap:Fault");
            Node_S detail, eCode= null;
            if(soapenv12.equals(soapNS)){
            	eCode = 
            	fault
                    .append("soap:Code")
                        .append("soap:Value")
                            .text("soap:Receiver")
                        .end();
            	
            	Throwable ep = extractExceptionCause(throwable);

            	if(ep instanceof ESoap.EParameterizedException){
            		
            	}
            		detail = 
                    eCode.end()
                    .append("soap:Reason")
                        .append("soap:Text")
                            .text(getCause(throwable).toString())
                        .end()
                    .end()
                    .append("soap:Detail");
                    detail.end();
            }
            else{
        		detail = 
                fault
                    .append("faultcode")
                        .text("soap:Server")
                    .end()
                    .append("faultstring")
                        .text(getCause(throwable).toString())
                    .end()
                    .append("detail");
                    detail.end();
            }
            
        	Throwable ep = extractExceptionCause(throwable);
            if(ep instanceof ESoap.EParameterizedException){
            	if(null != eCode){
            		ESoap.EParameterizedException ep1 = (EParameterizedException) ep;
            		eCode.append("soap:Subcode")
	                	.append("soap:Value")
	                		.text(ep1.getSubcode())
	                	.end()
	                .end();
            	}
            }
            	
            if(null != method && !method.isEmpty()){
                Operation op = findByName(desc.getOperations(), method);
                if(null == op)
                	throw new NoSuchElementException(method);
                PropDef prop = op.getThrowByType(ep.getClass());
                if(null != prop)
                	detail.append(new TypedObject(prop, ep), new ObjectConverter(desc, m_targetNamespace));
            }
            
            doc = body.getDocument();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return doc;
    }
    
    private Throwable extractExceptionCause(Throwable throwable) {
    	Throwable ep=throwable;
    	while((ep instanceof InvocationTargetException || ep instanceof RethrownException) && null != ep.getCause()){
    		ep = ep.getCause();
    	}
    	
    	return ep;
	}

	private Throwable getCause(Throwable throwable) {
    	while(null != throwable.getCause())
    		throwable = throwable.getCause();
		return throwable;
	}

	/*
    private String getMsgString(Throwable e) throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out));
        return new String(out.toByteArray(), "utf-8");
    }*/

    public void writeDocument(OutputStream output, Document doc) throws TransformerException {
        getTransformer().transform(new DOMSource(doc), new StreamResult(output));
    }
    
    public InterfaceDescription parse(InterfaceDescription oldDesc, List<Class> interfaces) throws IntrospectionException {
    	ArrayList<InterfaceOperationDescription> ops = new ArrayList<InterfaceOperationDescription>();
    	List<SoapInclude> soapIncs = new ArrayList<SoapInclude>();
    	for(Class cls : interfaces){
    		ops.addAll(listInterfaceOperations(cls, true));
    		SoapInclude anno = (SoapInclude) cls.getAnnotation(SoapInclude.class);
    		if(null != anno){
    			soapIncs.add(anno);
    		}
    		
    	}
    	return parse(oldDesc, ops, getClass().getPackage(), soapIncs);
    }

	public InterfaceDescription parse(InterfaceDescription oldDesc, 
			List<InterfaceOperationDescription> operationDescs, Package pkg, List<SoapInclude> soapIncs) {
    	
        InterfaceDescription res = new InterfaceDescription();
        ClassParser parser = new ClassParser(pkg, m_customSerializers);
        parser.addNonParsedParent(EParameterizedException.class);
        TypeDefLibCache lib = new TypeDefLibCache(parser, m_customSerializers);

        for(SoapInclude soapInc : soapIncs){
            for(Class extraCls : soapInc.extraClasses()){
                lib.add(extraCls);
            }
        }
        
        for (InterfaceOperationDescription opDesc : operationDescs) {
            res.getOperations().add(parseOperation(opDesc, lib));
        }
        
        List<TypeDef> typeDefs = lib.getDefs();
        if(null != oldDesc){
            // Order operations
            Order.orderINames(res.getOperations(), oldDesc.getOperations());
            // Order types
            Order.orderINames(typeDefs, oldDesc.getTypes());
            // Order properties in ClassDefs 
            Iterator<TypeDef> iOldDef = oldDesc.getTypes().iterator();
            for (TypeDef def : typeDefs) {
                ClassDef oldDef = null;
                while(iOldDef.hasNext()){
                    TypeDef def2 = iOldDef.next();
                    if(def2.getName().equals(def.getName())){
                        if(def2 instanceof ClassDef)
                            oldDef = (ClassDef) def2;
                        break;
                    }
                }
                if(def instanceof ClassDef && oldDef != null){
                    ClassDef cDef = (ClassDef) def;
                    Order.orderINames(cDef.getOwnProps(), oldDef.getOwnProps());                        
                }                    
            }
        }
        res.getTypes().addAll(typeDefs);
        return res;

	}
	
	public static class ValErrItem{
		
		public static class EPropSeqNotFound extends ValErrItem{

			public EPropSeqNotFound(String prop) {
				super(prop);
			}
			
		}

		public static class ENoPropInSequence extends ValErrItem{

			private String m_prop;

			public ENoPropInSequence(String type, String prop) {
				super(type);
				m_prop = prop;
			}
			@Override
			public boolean equals(Object obj) {
				return super.equals(obj) && m_prop.equals(((ENoPropInSequence)obj).m_prop);
			}

			@Override
			public String toString() {
				return String.format("Type: %s, prop: %s", m_type, m_prop);
			}

		}
		
		public static class ENoPropDefForSequence extends ValErrItem{

			private String m_prop;

			public ENoPropDefForSequence(String type, String prop) {
				super(type);
				m_prop = prop;
			}
			@Override
			public boolean equals(Object obj) {
				return super.equals(obj) && m_prop.equals(((ENoPropDefForSequence)obj).m_prop);
			}
			
			@Override
			public String toString() {
				return String.format("Type: %s, prop: %s", m_type, m_prop);
			}
			
		}

		protected String m_type;
		
		public ValErrItem(String prop) {
			m_type = prop;
		}
		
		@Override
		public boolean equals(Object obj) {
			ValErrItem other = (ValErrItem) obj; 
			return getClass().equals(obj.getClass()) && m_type.equals(other.m_type);
		}
		
		@Override
		public String toString() {
			return String.format("Type: %s", m_type);
		}
	}
	
	public List<ValErrItem> validateInterface(Class key) throws IntrospectionException{
		return validateInterface(listInterfaceOperations(key, false), key.getPackage(), (SoapInclude) key.getAnnotation(SoapInclude.class));
	}
	
	public List<ValErrItem> validateInterface(List<InterfaceOperationDescription> ops, Package pkg, SoapInclude soapInc){
		ArrayList<ValErrItem> res = new ArrayList<ValErrItem>();

		List<SoapInclude> incs = null == soapInc ? Collections.EMPTY_LIST : Arrays.asList(soapInc);
		InterfaceDescription desc = parse(null, ops, pkg, incs);
		for(TypeDef td : desc.getTypes()){
			if(td instanceof ClassDef){
				ClassDef cd = (ClassDef) td;
				try {
					if(!cd.getOwnProps().isEmpty()){
						Class<?> cls = Class.forName(cd.getType());
						PropertiesSequence seq = cls.getAnnotation(PropertiesSequence.class);
						if(null == seq)
							res.add(new ValErrItem.EPropSeqNotFound(td.getName()));
						else{
							HashSet<String> declared = new HashSet<String>();
							for(PropDef pd : cd.getOwnProps()){
								declared.add(pd.getName());
							}
							HashSet<String> seqSet = new HashSet<String>(Arrays.asList(seq.sequence()));
							seqSet.removeAll(declared);
							for(String pname : seqSet){
								res.add(new ValErrItem.ENoPropDefForSequence(td.getName(),pname));								
							}
							
							declared.removeAll(Arrays.asList(seq.sequence()));
							for(String pname : declared){
								res.add(new ValErrItem.ENoPropInSequence(td.getName(),pname));								
							}
						}
					}
						
				} catch (ClassNotFoundException e) {
					ApiAlgs.rethrowException(e);
				}
			}
		}
		return res;
	}
	
	public static class JsonResult extends JsonRpc{
		private static final long serialVersionUID = -339769101120563001L;
		private Object m_result;

		public JsonResult() {
			super("2.0", 1);
		}

		public Object getResult() {
			return m_result;
		}

		public void setResult(Object result) {
			m_result = result;
			put("result", result);
		}
		
	} 
	
	public String serializeJson(InterfaceDescription desc, String method, Object response) throws IOException {
		JSONSerializer js = new JSONSerializer();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonResult jsonRes = new JsonResult();
		jsonRes.setResult(response);
		js.serializeObject(jsonRes, out);
		return new String(out.toByteArray(), "utf-8");
	}

	public String serializeJsonException(Throwable e) throws IOException {
		JSONSerializer js = new JSONSerializer();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonRpcError jsonRes = new JSONSerializer.JsonRpcError("2.0", 1, exceptionToError(e));
		js.serializeObject(jsonRes, out);
		return new String(out.toByteArray(), "utf-8");
	}

	public static int HTTP_ERROR_METHOD_NOT_FOUND = 405;
	public static int HTTP_ERROR_BAD_REQUEST = 400;
	public static int HTTP_ERROR_SERVER_INTERNAL = 500;
	
	public static Error exceptionToError(Throwable e) {
		e = EUtils.unwrap(e);
		
		int code; 
		if(e instanceof ESoap.EMethodNotFound)
			code = HTTP_ERROR_METHOD_NOT_FOUND;
		else if(e instanceof ESoap.EUnknownElement)
			code = HTTP_ERROR_BAD_REQUEST; // Bad request
		else if(e instanceof JsonParserException)
			code = HTTP_ERROR_BAD_REQUEST;
		else 
			code = HTTP_ERROR_SERVER_INTERNAL;
		StringWriter strBuffer = new StringWriter();
		PrintWriter writer = new PrintWriter(strBuffer);
		e.printStackTrace(writer);
		writer.close();
		return new JSONSerializer.JsonRpcError.Error(code, e.getClass().getName() + ": " + e.getMessage(), "");
	}

	public <T> void addCustomSerializer(CustomSerializer iCustomSerializer) {
		m_customSerializers.add(iCustomSerializer);
	}

	public void serializeToStream(OutputStream output, InterfaceDescription desc, SOAPDocument soap) 
			throws TransformerConfigurationException, TransformerException, JAXBException{
		Source src = createSource(desc, soap);
		getTransformer().transform(src, new StreamResult(output));
	}

	private Source createSource(InterfaceDescription desc, SOAPDocument soap) throws JAXBException {
//		JAXBContext jc = JAXBContext.newInstance(SOAPDocument.class);
		
//	    Marshaller m = jc.createMarshaller();
		InputSource inpSrc = new InputSource();
		SAXSource sax = new SAXSource(inpSrc);
		
		return sax;
	}

	public Map<String, Boolean> getConfiguration() {
		return m_configuration;
	}

}
