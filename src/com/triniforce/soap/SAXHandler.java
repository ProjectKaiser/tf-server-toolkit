/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.triniforce.soap.ESoap.EInterfaceElementException;
import com.triniforce.soap.InterfaceDescription.MessageDef;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.MapDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IName;

public class SAXHandler {
    static class CurrentObject{
        TypeDef  m_objDef;
        int      m_propIdx;
        Object[] m_props;
        private boolean m_bNull;
        
        static final Object DEFAULT_OBJECT = new Object();
        
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
            else{
                m_bNull = bNull;
                
                setType(objDef);
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
                        throw new ESoap.EElementReentry(propName);
                    }
                    return cd.getProps().get(m_propIdx).getType();
                }
                else
                	throw new ESoap.EUnknownElement(cd.getType() + "." + propName);
            }
            else if (m_objDef instanceof ArrayDef){
                ArrayDef ad = (ArrayDef) m_objDef;
                if(ad.getPropDef().getName().equals(propName)){
                    return ad.getPropDef().getType();
                }
            }
            
            ApiAlgs.assertNotNull(m_objDef, propName);
            
            throw new ESoap.EUnknownElement(propName);
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
    
        public void setType(TypeDef td){
        	m_objDef = td;

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
    
    public SAXHandler(InterfaceDescription desc) {
        m_desc = desc;
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

}
