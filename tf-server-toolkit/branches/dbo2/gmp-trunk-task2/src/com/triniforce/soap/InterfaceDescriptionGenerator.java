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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.triniforce.soap.InterfaceDescription.MessageDef;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.InterfaceDescriptionGenerator.ObjectConverter.TypedObject;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.TypeDef;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.EnumDef;
import com.triniforce.soap.TypeDef.MapDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.WsdlDescription.WsdlMessage;
import com.triniforce.soap.WsdlDescription.WsdlType;
import com.triniforce.soap.WsdlDescription.WsdlTypeElement;
import com.triniforce.soap.WsdlDescription.WsdlPort.WsdlOperation;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IName;

public class InterfaceDescriptionGenerator {
    
    private String m_targetNamespace;
    private String m_serviceName;
    
    private SAXParserFactory m_SAXParserFactory;
    private DocumentBuilderFactory m_documentBuilderFactory;
    private TransformerFactory m_transformerFactory;

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
        InterfaceDescription res = new InterfaceDescription();
        try {
            BeanInfo info = Introspector.getBeanInfo(cls);
            TypeDefLibCache lib = new TypeDefLibCache(new ClassParser(pkg));
            for (MethodDescriptor mDesc : info.getMethodDescriptors()) {
                res.getOperations().add(parseOperation(mDesc, lib));
            }
            SoapInclude soapInc = cls.getAnnotation(SoapInclude.class);
            if(null != soapInc){
                for(Class extraCls : soapInc.extraClasses()){
                    lib.add(extraCls);
                }
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
        } catch (IntrospectionException e) {
            ApiAlgs.rethrowException(e);
        }
        return res;
    }
    
    private Operation parseOperation(MethodDescriptor mDesc, TypeDefLibCache lib) {
        MessageDef inMsgType = new InterfaceDescription.MessageDef(mDesc.getName());
        int i=0;
        for(Type type : mDesc.getMethod().getGenericParameterTypes()){
            TypeDef def = lib.add(type);
            inMsgType.addParameter("arg"+i++, TypeDefLibCache.toClass(type), def);
        }
        
        MessageDef outMsgType = new MessageDef(mDesc.getName() + "Response");
        Type retType = mDesc.getMethod().getGenericReturnType();
        if(!retType.equals(Void.TYPE)){
            outMsgType.addParameter(mDesc.getName()+"Result", TypeDefLibCache.toClass(retType), lib.add(retType));
        }
        
        return new Operation(mDesc.getName(), inMsgType, outMsgType);
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
        public TypeConverter(boolean bShowName) {
            m_bShowName = bShowName;   
        }
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
	                    public void run(Node_S parent, WsdlTypeElement val) {
	                        int maxOccur = val.getMaxOccur();
	                        String sMaxOccur = maxOccur == -1 ? "unbounded" : Integer.toString(maxOccur);
	                        Node_S e = parent.append("s:element")
	                            .attr("minOccurs", Integer.toString(val.getMinOccur()))
	                            .attr("maxOccurs", sMaxOccur)
	                            .attr("name", val.getName());
	                        if(!val.isResidentType()){
	                            e.append(val.getType(), new TypeConverter(false));
	                        }
	                        else{
	                            if(!val.getType().getTypeDef().getType().equals(Object.class.getName()))
	                                e.attr("type", getTypeName(val.getType()));                            
	                        }
	                    }
	                })
	                .end()
	            .end();
            }
            List<String> restrBase = val.getResriction();
            if(null != restrBase){
            	Node_S e = t.append("s:restriction").attr("base", "s:string");
            	for (String value : restrBase) {
            		e.append("s:enumeration").attr("value", value);
				}	
            }
        }
        private String getTypeName(WsdlType type) {
            String res = type.getTypeDef().getName();
            if(type.getTypeDef() instanceof ScalarDef && 
            		(!(type.getTypeDef() instanceof EnumDef))){
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
            ApiAlgs.assertEquals(null,
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
                            public void run(Node_S parent, WsdlTypeElement val) {
                                TypeConverter tc = new TypeConverter(false);
                                parent.append("s:element")
                                    .attr("name", val.getName())
                                    .append(val.getType(), tc)
                                .end();
                            }
                        })
                        .append(desc.getWsdlTypes(), new TypeConverter(true))
                    .end()
                .end()
                .append(desc.getMessages(), new IConverter<WsdlMessage>(){
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
                .append("wsdl:portType")
                    .attr("name", m_serviceName + "Soap")
                    .append(desc.getWsdlPort().getOperations(), new IConverter<WsdlOperation>(){
                        public void run(Node_S parent, WsdlOperation val) {
                            parent.append("wsdl:operation")
                                .attr("name", val.getName())
                                .append("wsdl:input")
                                    .attr("message", "tns:"+val.getInput().getMessageName())
                                .end()
                                .append("wsdl:output")
                                    .attr("message", "tns:"+val.getOutput().getMessageName())
                                .end()
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

    static class SoapHandler extends DefaultHandler{
        static class CurrentObject{
            TypeDef  m_objDef;
            int      m_propIdx;
            Object[] m_props;
            private boolean m_bNull;
            
            static final Object DEFAULT_OBJECT = new Object();
            
            public CurrentObject(TypeDef objDef) throws ESoap.ENonNullableObject {
                this(objDef, false);
            }
            
            public CurrentObject(TypeDef objDef, boolean bNull) throws ESoap.ENonNullableObject{
                if(null == objDef)
                    throw new NullPointerException("objDef");
                if(bNull){
                    if(!objDef.isNullable())
                        throw new ESoap.ENonNullableObject(objDef.getType());
                }
                else{
                    m_objDef = objDef;
                    m_bNull = bNull;
                    
                    if(m_objDef instanceof ClassDef){
                        m_props = new Object[((ClassDef)m_objDef).getProps().size()];
                        Arrays.fill(m_props, DEFAULT_OBJECT);
                    }
                    else if (m_objDef instanceof MapDef){
                        m_props = new Object[]{new HashMap<Object, Object>()};
                    }
                    else if (m_objDef instanceof ArrayDef){
                        m_props = new Object[]{new ArrayList<Object>()};
                    }
                    else if (m_objDef instanceof ScalarDef){
                    	ScalarDef sd = (ScalarDef) m_objDef;
                    	if(sd.getType().equals(String.class.getName()))
                    		m_props = new Object[]{""};
                    	else
                    		m_props = null;
                    }
                }
            }

            @SuppressWarnings("unchecked")
            Object toObject(){
                Object res = null;
                if(!m_bNull){
                    if(m_objDef instanceof ClassDef){
                        ClassDef cd = (ClassDef) m_objDef;
                        try {
                            Class<?> cls = Class.forName(cd.getType());
                            Object instance;
                            if(cls.isArray())
                                instance = Array.newInstance(Object.class, m_props.length);
                            else
                                instance = cls.newInstance();
                            res = instance;
                            for (int i=0; i<cd.getProps().size(); i++) {
                                PropDef propDef = cd.getProps().get(i);
                                Object val = m_props[i];
                                if(!DEFAULT_OBJECT.equals(val))
                                	propDef.set(instance, val);
                            }
                        }catch(InstantiationException e){
                        	ApiAlgs.rethrowException(cd.getType().toString(), e);
                        }catch (Exception e) {
                            ApiAlgs.rethrowException(e);
                        }
                    }
                    else{
                        if(null != m_props)
                            res = m_props[0];
                    }
                }
                return res;
            }

            TypeDef setCurrentProp(String propName) throws ESoap.EUnknownElement, ESoap.EElementReentry{
                if(m_objDef instanceof ClassDef){
                    ClassDef cd = (ClassDef) m_objDef;
                    m_propIdx = getPos(cd.getProps(), propName);
                    if(m_propIdx !=-1){
                        Object v = m_props[m_propIdx];
                        if(!DEFAULT_OBJECT.equals(v)){
                            throw new ESoap.EElementReentry(new QName(propName));
                        }
                        return cd.getProps().get(m_propIdx).getType();
                    }
                }
                else if (m_objDef instanceof ArrayDef){
                    ArrayDef ad = (ArrayDef) m_objDef;
                    if(ad.getPropDef().getName().equals(propName)){
                        return ad.getComponentType();
                    }
                }
                
                throw new ESoap.EUnknownElement(new QName(propName));
            }
            
            @SuppressWarnings("unchecked")
            void setPropValue(Object value){
                if(m_objDef instanceof ClassDef){
                    m_props[m_propIdx] = value;
                }
                else if (m_objDef instanceof ArrayDef){
                    ArrayDef ad = (ArrayDef) m_objDef;
                    ad.getPropDef().set(m_props[0], value);
                }
                else
                    throw new RuntimeException("ScalarDef");
            }
            
            Object[] getProps(){
                return m_props;
            }

            public void setStringValue(String value) {
                if(null == value)
                    throw new NullPointerException();
                if(m_objDef instanceof ScalarDef){
                    if(null == m_props || (null != m_props && value.length() != 0)){
                        Object res = null;
                        ScalarDef sd = (ScalarDef)m_objDef;
                        res = sd.valueOf(value);
                        
                        m_props = new Object[]{res};
                    }
                }
            }
            
            private <T extends IName> int getPos(List<T> col, String tag) {
                for (int i=0; i<col.size(); i++) {
                    if(col.get(i).getName().equals(tag))
                        return i;
                }
                return -1;
            }
        }
        
        static class FaultMessage{
        	String m_string;
        	String m_code;
        }
        
        Stack<QName> m_stk = new Stack<QName>();
        private String m_tns;
        Stack<CurrentObject> m_objStk = new Stack<CurrentObject>(); 
        private InterfaceDescription m_desc;
        //private ClassDef m_methodDef;
        private HashMap<String, String> m_uriMap;
        
        private SOAPDocument m_result;
        private String m_stringValue;
        
        private FaultMessage m_fault=null;
        
        public SoapHandler(String tns, InterfaceDescription desc) {
            m_tns = tns;
            m_desc = desc;
            m_uriMap = new HashMap<String, String>();
        }
        
        @Override
        public void startElement(String ns, String tag, String arg2, Attributes attrs) throws SAXException {
            QName qn = new QName(ns, tag);
            
            if(m_stk.isEmpty()){
                // must be soap
                if(soapenv.equals(ns) || soapenv12.equals(ns)){
                    m_result = new SOAPDocument();
                    m_result.m_soap = ns;
                }
                else{
                    throw new ESoap.EUnknownElement(qn);
                }
            }
            
            if(ns.equals(m_result.m_soap)){
                if(tag.equals("Envelope")){
                    if(!m_stk.isEmpty())
                        throw new ESoap.EUnknownElement(qn);
                }
                else if(tag.equals("Body")){
                    if(!m_stk.peek().equals(new QName(m_result.m_soap, "Envelope")))
                        throw new ESoap.EUnknownElement(qn);
                    if(null != m_result.m_method)
                        throw new ESoap.EElementReentry(qn);
                }
                else if(tag.equals("Fault")){
                    if(!m_stk.peek().equals(new QName(m_result.m_soap, "Body")))
                        throw new ESoap.EUnknownElement(qn);
                    m_fault = new FaultMessage();
                }
            }
            else if(ns.equals(m_tns)){
                CurrentObject co;
                boolean bNull = isNull(attrs);
                if(m_stk.peek().equals(new QName(m_result.m_soap, "Body"))){
                    if(null != m_result.m_method)
                        throw new ESoap.EElementReentry(qn);
                    MessageDef methodDef = getMethodDef(tag);
                    co = new CurrentObject(methodDef, bNull);
                }
                else{
                    co = m_objStk.peek();
                    TypeDef typeDef = co.setCurrentProp(tag);
                    String typeName = attrs.getValue(xsi, "type");
                    if(null != typeName){
                        QName qName = getQName(typeName);
                        TypeDef reqTypeDef = m_desc.getTypeDef(qName.getLocalPart(), !m_tns.equals(qName.getNamespaceURI()));
                        if(!isSubtype(typeDef, reqTypeDef)){
                        	throw new ESoap.EWrongElementType(qn, typeName);
                        }
                        typeDef = reqTypeDef;
                    }
                    co = new CurrentObject(typeDef, bNull);
                }
                m_objStk.push(co);
                m_stringValue = null;
            }
            else{
            	if(!(m_fault != null && 
            			("faultcode".equals(tag) || "faultstring".equals(tag) || "detail".equals(tag))))
            		throw new ESoap.EUnknownElement(qn);
            }
            
            m_stk.push(qn);
        }
        

        private boolean isSubtype(TypeDef parentDef, TypeDef childDef) {
        	if(childDef.getName().equals(parentDef.getName()))
        		return true;
        	if(parentDef.getType().equals(Object.class.getName()))
        		return true;
        	if(!(parentDef instanceof ClassDef) || !(childDef instanceof ClassDef))
        		return false;
        	ClassDef child = (ClassDef) childDef;
        	ClassDef parent =  (ClassDef) parentDef;
        	boolean res=false;
        	while(null != child && !(res = child.getType().equals(parent.getType()))){
        		child = child.getParentDef();
        	}
        	return res;
		}

		private QName getQName(String typeName) {
            int iDiv = typeName.indexOf(':');
            String prefix = "";
            String local = typeName;
            if(iDiv!=-1){
                prefix = typeName.substring(0, iDiv);
                local = typeName.substring(iDiv+1);
            }
            QName res = new QName(m_uriMap.get(prefix), local, prefix);
            return res;
        }

        private boolean isNull(Attributes attrs) {
            boolean res = false;
            String strNull = attrs.getValue(xsi, "nil");
            if(null != strNull){
                res = strNull.equals("true");
            }
            return  res;
        }

        private MessageDef getMethodDef(String tag) throws ESoap.EMethodNotFound {
            MessageDef res = null;
            for (Operation op : m_desc.getOperations()) {
                if(op.getName().equals(tag)){
                    m_result.m_method = op.getName();
                    m_result.m_bIn = true;
                    res = op.getRequestType();
                }
                else if((op.getName() + "Response").equals(tag)){
                    m_result.m_method = op.getName();
                    m_result.m_bIn = false;
                    res = op.getResponseType();
                }
            }
            if(null ==  res)
                throw new ESoap.EMethodNotFound(new QName(m_tns, tag));
            return res;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
//            if(!m_objStk.isEmpty()){
            	String vStr = new String(ch, start, length).trim();
            	if(null != m_stringValue)
            		m_stringValue += "\n"+vStr;
            	else
            		m_stringValue = vStr;
            	
//                CurrentObject obj = m_objStk.peek();
//                String str = new String(ch, start, length);
//                obj.setStringValue(str.trim());
//            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(uri.equals(m_tns)){
                CurrentObject obj = m_objStk.pop();
                if(m_objStk.isEmpty()){
                    m_result.m_args = (Object[]) obj.toObject();
                }
                else{
                	if(null != m_stringValue)
                		obj.setStringValue(m_stringValue);
                    CurrentObject parent = m_objStk.peek();
                    parent.setPropValue(obj.toObject());
                }
            }
            if(m_fault != null){
            	if("faultstring".equals(localName)){
            		m_fault.m_string = m_stringValue.trim();
            	}
            }
        	m_stringValue = null;
            m_stk.pop();
        }
        
        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            m_uriMap.put(prefix, uri); 
            super.startPrefixMapping(prefix, uri);
        }
    }
    
    public static class SOAPDocument{
        public String   m_soap;
        public String   m_method;
        public Object[] m_args;
        public boolean  m_bIn;
    }

    public SOAPDocument deserialize(InterfaceDescription desc, InputStream source) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = getParser();
        SoapHandler h = new SoapHandler(m_targetNamespace, desc);
        parser.parse(new InputSource(source), h);
        if(null != h.m_fault)
        	throw new ESoap.EFaultCode(h.m_fault.m_string);
        SOAPDocument res = h.m_result;
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

    public Document serializeException(String soapNS, Throwable throwable) {
        Document doc = null;
        try {
        	if(null == soapNS)
        		soapNS = soapenv;
            Node_S body = createSoapDocument(soapNS);
            Node_S fault = body.append("soap:Fault"); 
            if(soapenv12.equals(soapNS)){
                fault
                    .append("soap:Code")
                        .append("soap:Value")
                            .text("soap:Receiver")
                        .end()
                    .end()
                    .append("soap:Reason")
                        .append("soap:Text")
                            .text(getCause(throwable).toString())
                        .end()
                    .end()
                    .append("soap:Detail")
                    .end();
            }
            else{
                fault
                    .append("faultcode")
                        .text("soap:Server")
                    .end()
                    .append("faultstring")
                        .text(getCause(throwable).toString())
                    .end()
                    .append("detail")
                    .end();
            }
            doc = body.getDocument();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return doc;
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
    

}
