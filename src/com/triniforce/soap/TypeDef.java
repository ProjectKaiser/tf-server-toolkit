/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.output.WriterOutputStream;

import com.triniforce.soap.TypeDefLibCache.MapDefLib.MapComponentDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiAlgs.SimpleName;
import com.triniforce.utils.Base64;
import com.triniforce.utils.Base64.OutputStream;
import com.triniforce.utils.TFUtils;

public class TypeDef extends SimpleName{
    private static final long serialVersionUID = 6626564048671748844L;

    public static class ScalarDef extends TypeDef{
        private static final long serialVersionUID = 1L;
        
        static HashMap<String, String>   BOXES;
        static HashMap<String, String> SCALAR_NAMES;
        static{
            BOXES = new HashMap<String, String>();
            BOXES.put(boolean.class.getName(), Boolean.class.getName());
            BOXES.put(int.class.getName(), Integer.class.getName());
            BOXES.put(long.class.getName(), Long.class.getName());
            BOXES.put(short.class.getName(), Short.class.getName());
            BOXES.put(float.class.getName(), Float.class.getName());
            BOXES.put(double.class.getName(), Double.class.getName());
            
            SCALAR_NAMES = new HashMap<String, String>();
            SCALAR_NAMES.put(Boolean.class.getName(), "boolean");
            SCALAR_NAMES.put(Integer.class.getName(), "int");
            SCALAR_NAMES.put(Long.class.getName(), "long");
            SCALAR_NAMES.put(Short.class.getName(), "short");
            SCALAR_NAMES.put(Float.class.getName(), "float");
            SCALAR_NAMES.put(Double.class.getName(), "double");
            SCALAR_NAMES.put(String.class.getName(), "string"); 
            SCALAR_NAMES.put(Object.class.getName(), "object");
            SCALAR_NAMES.put(Date.class.getName(), "dateTime");
            SCALAR_NAMES.put(BigDecimal.class.getName(), "decimal");
            SCALAR_NAMES.put(byte[].class.getName(), "base64Binary");
        }
        
        boolean m_bNillable;
        
        
        public ScalarDef(Class type) {
            this(scalarName(type.getName()), type, !BOXES.containsKey(type.getSimpleName()));
        }
        
        public ScalarDef(String name, Class type, boolean bNill) {
            super(name, type);
            m_bNillable = bNill;
        }

        public static String scalarName(String typeName) {
            String box = BOXES.get(typeName);
            if(null != box)
                typeName = box;
            return SCALAR_NAMES.get(typeName);
        }
        
        public static Collection<String> scalarNames(){
        	return SCALAR_NAMES.values();
        }
        
        @Override
        boolean isNullable() {
            return m_bNillable;
        }
        
        boolean isBoxType(){
        	return BOXES.containsKey(getType());
        }

        public Object valueOf(String value) {
            Object res;
            String typeName = getName();
            if(typeName.equals("string"))
            	res = value;
            else{
	            value = value.trim();
	            if(typeName.equals("int"))
	                res = Integer.valueOf(value);
	            else if(typeName.equals("short"))
	                res = Short.valueOf(value);
	            else if(typeName.equals("long"))
	                res = Long.valueOf(value);
	            else if(typeName.equals("float"))
	                res = Float.valueOf(value);
	            else if(typeName.equals("double"))
	                res = Double.valueOf(value);
	            else if(typeName.equals("boolean")){
	                res = "1".equals(value) || "true".equals(value);
	            }
	            else if(typeName.equals("dateTime")){
	            	DatatypeFactory tf;
					try {
						tf = DatatypeFactory.newInstance();
		            	XMLGregorianCalendar gregCal = tf.newXMLGregorianCalendar(value);
		            	return gregCal.toGregorianCalendar().getTime();
					} catch (DatatypeConfigurationException e) {
						ApiAlgs.rethrowException(e);
						res = null;
					}
	            }
	            else if(typeName.equals("decimal")){
	            	res = new BigDecimal(value == null ? value : value.replace(",", "."));
	            }
	            else if(typeName.equals("base64Binary")){
	            	res = Base64.decode(value);
	            }
	            else
	                res = value;
            }
            return res;
        }
        
        public String stringValue(Object v){
        	StringWriter sw = new StringWriter();
        	serialize(v, sw);
        	return sw.toString();
        }
        
        public void serialize(Object v, Writer w){
        	try{
	        	if(getName().equals("dateTime")){
	        		try {
	    				GregorianCalendar gregCal = new GregorianCalendar();
		        		gregCal.setTimeZone(TimeZone.getTimeZone("GMT"));
		        		gregCal.setTime((Date) v);
						DatatypeFactory tf = DatatypeFactory.newInstance();
						w.append(tf.newXMLGregorianCalendar(gregCal).toXMLFormat());
					} catch (DatatypeConfigurationException e) {
						ApiAlgs.rethrowException(e);
					}
	        	}
	        	else if(getName().equals("base64Binary")){
	        		WriterOutputStream wout = new org.apache.commons.io.output.WriterOutputStream(w, Charset.forName("utf-8"));
	        		OutputStream b64 = new Base64.OutputStream(wout);
	        		b64.write((byte[]) v);
	        		b64.close();
	        		wout.close();
	        	}
	        	else
	        		w.append(v.toString());
        	} catch (IOException e){
        		throw new ApiAlgs.RethrownException(e);
        	}
        	
        }
        
    }
    
    static class ArrayDef extends TypeDef implements Serializable{
        private static final long serialVersionUID = -8210579239652637815L;
        static final String PROP_NAME = "value"; 
        static class ArrayGetSet implements PropDef.IGetSet, Serializable{
            private static final long serialVersionUID = 3898535417848742395L;

            public Collection get(final Object obj) {
                if(List.class.isInstance(obj))
                    return (List) obj;
                if(obj.getClass().isArray()){
                    return new AbstractList<Object>(){
                        @Override
                        public Object get(int arg0) {
                            return Array.get(obj, arg0);
                        }
                        @Override
                        public int size() {
                            return Array.getLength(obj);
                        }
                    };
                }
                TFUtils.assertTrue(false, "");
                return null;
            }
            
            @SuppressWarnings("unchecked")
            public void set(Object obj, Object value) {
                Collection<Object> col = (Collection<Object>) obj;
                col.add(value);
            }
        } 
        
        PropDef m_propDef;
        
        public ArrayDef(String name, Class type, TypeDef componentDef) {
            this(name, type, componentDef, new ArrayGetSet());
        }
        
        protected ArrayDef(String name, Class type, TypeDef compDef,  PropDef.IGetSet getSet) {
            super(name, type);
            m_propDef = new PropDef(PROP_NAME, compDef, type.getName(), getSet);
        }
        
        public TypeDef getComponentType() {
            return m_propDef.getType();
        }
        public PropDef getPropDef() {
            return m_propDef;
        } 
    }
    
    public static class ClassDef extends TypeDef  implements Serializable{
        private static final long serialVersionUID = -2276851356973032125L;

        static class EMethodNotFound extends RuntimeException{
            private static final long serialVersionUID = 6817399823955558597L;

            public EMethodNotFound(String mName) {
                super(mName);
            }
        }
        
        static class CDGetSet implements PropDef.IGetSet, Serializable{
            private static final long serialVersionUID = -7156241105779724042L;
            private String m_getterName;
            private String m_setterName;
            private String m_getterRawType;
            private String m_setterRawType;

            public CDGetSet(String rawType, String getterName, String setterName) {
                m_getterRawType = rawType;
                m_setterRawType = rawType;
                m_getterName = getterName;
                m_setterName = setterName;
            }
            public CDGetSet(String getterRawType, String getterName, String setterRawType, String setterName) {
                m_getterRawType = getterRawType;
                m_setterRawType = setterRawType;
                m_getterName = getterName;
                m_setterName = setterName;
            }
            
            public Object get(Object obj) {
                Method getter = getMethod(m_getterRawType, m_getterName);
                try {
                    return getter.invoke(obj, (Object[]) null);
                } catch (Exception e) {
                    ApiAlgs.rethrowException(e);
                    return null;
                }
            }

            private Method getMethod(String rawType, String name) {
                Method res = null;
                try {
                    Class<?> cls = Class.forName(rawType);
                    BeanInfo info = Introspector.getBeanInfo(cls);
                    for (MethodDescriptor mDesc : info.getMethodDescriptors()) {
                        if(mDesc.getName().equals(name)){
                        	res = mDesc.getMethod();
                  			break;
                        }
                    }
                } catch (Exception e) {
                    ApiAlgs.rethrowException(e);
                }
                if(null == res)
                    throw new EMethodNotFound(rawType + "." +name);
                return res;
            }

            public void set(Object obj, Object value) {
                Method setter = getMethod(m_setterRawType, m_setterName);
                try {
                    setter.invoke(obj, new Object[]{value});
                } catch(IllegalArgumentException e){
                	ApiAlgs.getLog(this).trace(obj.toString() + "."+m_setterName + "."+value);
                	throw e;
                } catch (Exception e) {
                    ApiAlgs.rethrowException(e);
                }
            }
            
        }
        
        List<PropDef> m_props = new ArrayList<PropDef>();
        private ClassDef m_parentDef;
        
        public ClassDef() {
            super();
        }
        
        public ClassDef(String name, Class type) {
            this(name, type, null);
        }
        
        public ClassDef(String name, Class type, ClassDef parent) {
            super(name, type);
            m_parentDef = parent;
        }

        public List<PropDef> getProps() {
            List<PropDef> res;
            if(null != m_parentDef && !m_parentDef.getProps().isEmpty()){
                res = new AbstractList<PropDef>(){
                    List<PropDef> m_parentProps = m_parentDef.getProps();
                    int m_parentSize = m_parentProps.size();
                    @Override
                    public PropDef get(int arg0) {
                        return arg0 < m_parentSize ? m_parentProps.get(arg0) : m_props.get(arg0-m_parentSize);
                    }
                    @Override
                    public int size() {
                        return m_parentSize + m_props.size();
                    }
                };
            }
            else{
                res = Collections.unmodifiableList(getOwnProps());
            }
                
            return res;
        }

        /**
         * Get property by name
         * @param string
         * @return
         */
        public PropDef getProp(String propName) {
            return InterfaceDescriptionGenerator.findByName(getProps(), propName);
        }

        public List<PropDef> getOwnProps() {
            return m_props;
        }
        
        public ClassDef getParentDef(){
            return m_parentDef;
        }
    }
    
    static class MapDef extends ArrayDef{
        private static final long serialVersionUID = 7432786089791434356L;
        static class MapGetSet implements PropDef.IGetSet, Serializable{
            private static final long serialVersionUID = -7882044528171234167L;
            public Collection get(Object obj) {
                Map map = (Map) obj;
                return map.entrySet();
            }
            @SuppressWarnings("unchecked")
            public void set(Object obj, Object value) {
                Map map = (Map) obj;
                Map.Entry entry = (Entry) value; 
                map.put(entry.getKey(), entry.getValue());
            }
        }

        public MapDef(String name, MapComponentDef componentDef) {
            super(name, Map.class, componentDef, new MapGetSet());
        }


        public TypeDef getKeyDef(){
            MapComponentDef compDef = (MapComponentDef) getComponentType();
            return compDef.getKeyDef();
        }
        public TypeDef getValueDef(){
            MapComponentDef compDef = (MapComponentDef) getComponentType();
            return compDef.getValueDef();
        }
    }
    
    static class EnumDef extends ScalarDef{
		private static final long serialVersionUID = -3782290546560925269L;
		private Class m_enumType;
		public EnumDef(String name, Class type) {
			super(String.class);
			setName(name);
			m_enumType = type;
		}
		String[] getPossibleValues(){
			Object[] consts = m_enumType.getEnumConstants();
			String[] res = new String[consts.length];
			for (int i = 0; i < consts.length; i++) {
				res[i] = consts[i].toString();
			}
			return res;
		}
		
		@Override
		public Object valueOf(String value) {
			try{
				return Enum.valueOf(m_enumType, value);
			} catch(IllegalArgumentException e){
				return null;
			}
		}
		    	
    }
    
    static class UnknownDef extends TypeDef{
		private static final long serialVersionUID = 5280677981465813206L;
    }

    private String m_typeName;
    
    public TypeDef() {
        super(null);
    }
    
    public TypeDef(String name, Class type) {
        super(name);
        m_typeName = null != type ? type.getName() : null;
    }
    
    public String getType(){
        return m_typeName;
    }

    boolean isNullable(){
        return true;
    }
}
