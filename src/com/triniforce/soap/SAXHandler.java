/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.triniforce.soap.ESoap.EInterfaceElementException;
import com.triniforce.soap.InterfaceDescription.MessageDef;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDef.UnknownDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;

public class SAXHandler {
	
	public static final String CFG_UNK_PROPS = "com.triniforce.soap.SAXHandler.UnknwonPropertties";

	Map<String, Boolean> m_configuration;
	
    static final UnknownDef UNKNOWN_DEF = new UnknownDef();
	
    class CurrentObject{
        TypeDef  m_objDef;
        Object[] m_props;
        private boolean m_bNull;
		private String m_name;
		private String m_currentPropName;
        
        public CurrentObject(String name, TypeDef objDef) throws ESoap.ENonNullableObject {
            this(name, objDef, false);
        }
        
        public CurrentObject(String name, TypeDef objDef, boolean bNull) throws ESoap.ENonNullableObject{
            if(null == objDef)
                throw new NullPointerException("objDef");
            if(bNull){
                if(!objDef.isNullable())
                    throw new ESoap.ENonNullableObject(name, objDef.getType());
            }
            //else{
                m_bNull = bNull;
                
                setType(objDef);
            //}
            m_name = name;
            //m_objDef = objDef;
        }

        @SuppressWarnings("unchecked")
        Object toObject(){
            Object res = null;
            if(!m_bNull){
            	res = m_objDef.instanciate(m_name, m_props);
            }
            if(res == null && !m_objDef.isNullable()){
            	throw new ESoap.ENonNullableObject(m_name, m_objDef.getType());
            }
            return res;
        }
        

        TypeDef setCurrentProp(String propName) throws ESoap.EUnknownElement, ESoap.EElementReentry{
        	PropDef prop = m_objDef.getProp(propName);
        	if(null == prop){
            	if(Boolean.TRUE.equals(m_configuration.get(CFG_UNK_PROPS)))
            		return UNKNOWN_DEF;
            	else
            		throw new ESoap.EUnknownElement(m_objDef.getType() + "." + propName);        		
        	}
        	m_currentPropName = propName;
        	return prop.getType();
        }
        
        @SuppressWarnings("unchecked")
        void setPropValue(Object value){
        	m_objDef.setPropValue(m_props, m_currentPropName, value);
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
    
        public void setType(TypeDef td){
        	m_objDef = td;
        	m_props = td.instanciateDefaultProperies();
        }

		public TypeDef getType() {
			return m_objDef;
		}
    }
    
    static class FaultMessage{
    	String m_string;
    	String m_code;
    }
    
    Stack<CurrentObject> m_objStk = new Stack<CurrentObject>(); 
    private InterfaceDescription m_desc;
    
//    SOAPDocument m_result;
    List<String> m_stringValue = new ArrayList<String>();
    
//    FaultMessage m_fault=null;
    
    String m_method;
    boolean m_bIn;
    Object[] m_args;
    
    public SAXHandler(InterfaceDescription desc, Map<String, Boolean> config) {
        m_desc = desc;
        m_configuration = config;
    }
    
    public TypeDef startElement(String tag, boolean bNull, TypeDef reqTypeDef) throws EInterfaceElementException {
        m_stringValue.clear();
        
        CurrentObject co;
        TypeDef res;
        if(m_objStk.isEmpty()){
            if(null != m_method)
                throw new ESoap.EElementReentry(tag);
            MessageDef methodDef = getMethodDef(tag);
            co = new CurrentObject(tag, methodDef, bNull);
            res = methodDef;
        }
        else{
            co = m_objStk.peek();
            TypeDef typeDef = co.setCurrentProp(tag);
            if(null != reqTypeDef){
                if(!isSubtype(typeDef, reqTypeDef)){
                	throw new ESoap.EWrongElementType(reqTypeDef.getName());
                }
                typeDef = reqTypeDef;
            }
            co = new CurrentObject(tag, typeDef, bNull);
            res = typeDef;
        }
        m_objStk.push(co);
        return res;
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

    private MessageDef getMethodDef(String tag) throws ESoap.EMethodNotFound {
        MessageDef res = null;
        for (Operation op : m_desc.getOperations()) {
            if(op.getName().equals(tag)){
                m_method = op.getName();
                m_bIn = true;
                res = op.getRequestType();
            }
            else if((op.getName() + "Response").equals(tag)){
                m_method = op.getName();
                m_bIn = false;
                res = op.getResponseType();
            }
        }
        if(null ==  res)
            throw new ESoap.EMethodNotFound(tag);
        return res;
    }

    public void characters(char[] ch, int start, int length){
    	String vStr = new String(ch, start, length);
    	m_stringValue.add(vStr);
    }
    
    public void endElement(){
        CurrentObject obj = m_objStk.pop();
        if(m_objStk.isEmpty()){
            m_args = (Object[]) obj.toObject();
        }
        else{
        	if(!m_stringValue.isEmpty())
        		obj.setStringValue(getNodeCharacters());
        	if(obj.getType() instanceof UnknownDef)
        		return ;
            CurrentObject parent = m_objStk.peek();
            parent.setPropValue(obj.toObject());
        }
    	m_stringValue.clear();
    }
    
    String getNodeCharacters() {
    	StringBuffer res = new StringBuffer();
    	Iterator<String> i = m_stringValue.iterator();
    	while(i.hasNext()){
    		String v = i.next();
			res.append(v);
    	}
		return res.toString();
	}

	public TypeDef getType(String typeName, boolean bScalar) {
		return m_desc.getTypeDef(typeName, bScalar);
	}

	public CurrentObject getTopObject() {
		return m_objStk.peek();
	}

	public CurrentObject createObject(String qn, TypeDef object) {
		return new CurrentObject(qn, object);
	}
	
	public CurrentObject createObject(String name, TypeDef objDef, boolean bNull) {
		return new CurrentObject(name, objDef, bNull);
	}

}
