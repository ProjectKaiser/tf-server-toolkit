/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.triniforce.soap.TypeDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.utils.ApiAlgs;

public class InterfaceDescription implements Serializable{
    private static final long serialVersionUID = 290040805900878471L;

    public static class Operation extends ClassDef  implements Serializable{
        private static final long serialVersionUID = 3333941812114708363L;
        
        public Operation() {
            super();
        }
        
        public Operation(String name, MessageDef reqType, MessageDef resType) {
            super(name, null);
            List<PropDef> props = getOwnProps();
            props.add(new PropDef(name, reqType, null, null));
            props.add(new PropDef(name+"Response", resType, null, null));
        }

        public MessageDef getRequestType() {
            return (MessageDef) getProps().get(0).getType();
        }
        public MessageDef getResponseType() {
            return (MessageDef) getProps().get(1).getType();
        }
    }
    
    static class MessageDef extends ClassDef implements Serializable{
        private static final long serialVersionUID = 3737063227692051644L;

        static class MsgPropGetSet implements PropDef.IGetSet, Serializable{
            private static final long serialVersionUID = -6536106193595685068L;
            
            private int m_index;

			private String m_prop;

            public MsgPropGetSet(String name, int index) {
                m_index = index;
                m_prop = name;
            }
            
            public Object get(Object obj) {
            	try{
            		return Array.get(obj, m_index);
            	} catch(IndexOutOfBoundsException e){
            		throw new NoSuchElementException(m_prop);  
            	}
            }

            public void set(Object obj, Object value) {
                Array.set(obj, m_index, value);
            }
            
        } 

        public MessageDef(String name) {
            super(name, Object[].class);
        }
        
        void addParameter(String name, Class rawType, TypeDef typeDef){
            List<PropDef> props = getOwnProps(); 
            props.add(new PropDef(name, typeDef, rawType.getName(), new MsgPropGetSet(name, props.size())));            
        }
        
    }
    
    private ArrayList<Operation> m_operations;
    private ArrayList<TypeDef>   m_types;
    
    
    public InterfaceDescription() {
        m_operations = new ArrayList<Operation>();
        m_types = new ArrayList<TypeDef>();
    }

    public List<Operation> getOperations() {
        return m_operations;
    }
    
    public List<TypeDef> getTypes() {
        return m_types;
    }

    public TypeDef getTypeDef(String name, boolean bScalar) {
        TypeDef res = null;
        if(bScalar){
            for(Entry<String, String> entry : TypeDef.ScalarDef.SCALAR_NAMES.entrySet()){
                if(entry.getValue().equals(name)){
                    try {
                        res = new TypeDef.ScalarDef(Class.forName(entry.getKey()));
                    } catch (ClassNotFoundException e) {
                        ApiAlgs.rethrowException(e);
                    }
                    break;
                }
            }
        }
        else{
            res = InterfaceDescriptionGenerator.findByName(getTypes(), name);
        }
        return res;
    }

    public WsdlDescription getWsdlDescription() {
        return new WsdlDescription(this);
    }

    public TypeDef getType(Class  cls) {
        TypeDef res = TypeDefLibCache.SCALAR_LIB.get(cls);
        if(null == res){
            for (TypeDef td : getTypes()) {
                if(td.getType().equals(cls.getName())){
                    res = td;
                    break;
                }
            }
        }
        return res;
    }

    public Operation getOperation(String opName) {
        return InterfaceDescriptionGenerator.findByName(getOperations(), opName);
    }
}
