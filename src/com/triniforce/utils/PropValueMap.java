/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.triniforce.server.select.EWrongParam;
import com.triniforce.server.soap.NamedVar;
import com.triniforce.utils.PropValueMap.ValueDef.Including;

public class PropValueMap {
    
    public static class EUnknownParam extends EWrongParam {
        private static final long serialVersionUID = -7117118085622679218L;

        public EUnknownParam(String paramName) {
            super(paramName); 
        }
    }
    
    public static class EInvalidTypeParam extends EWrongParam {
        private static final long serialVersionUID = -7117118085622679218L;

        public EInvalidTypeParam (String paramName) {
            super(paramName); 
        }
    }
    
    public static class EOnceIncludingParam extends EWrongParam {
        private static final long serialVersionUID = -3248793886236449669L;
        public EOnceIncludingParam (String paramName) {
            super(paramName); 
        }
    }
    
    public static class ENotNullableParam extends EWrongParam {
        private static final long serialVersionUID = -3248793886236449669L;
        public ENotNullableParam (String paramName) {
            super(paramName); 
        }
        @Override
        public String getMessage() {
            return MessageFormat.format("No parameter ''{0}''", getParamName()); //$NON-NLS-1$
        }
    }
    
    
    public static class ValueDef {
        public enum Including {NULLABLE, ONCE, ARRAY};        
        Class m_type;
        private Including m_inc;        
        
        public ValueDef(Class cls, Including including) {
            m_type = cls;
            m_inc = including;
        }
        public Class getType(){
            return m_type;
        }
        
        public Including getIncluding(){
            return m_inc;
        }
    }
    
    HashMap<String, Object> m_vals = new HashMap<String, Object>();

    public PropValueMap(NamedVar[] values, Map<String, ValueDef> defs, boolean bCaseSensitive) {
        for (NamedVar value : values) {
            String name = bCaseSensitive ? value.getName() : value.getName().toLowerCase(Locale.ENGLISH);
            putValue(defs, name, value.getValue());
            
        }
        checkOnceIncluding(defs);
    }

    private void checkOnceIncluding(Map<String, ValueDef> defs) {
        for (Entry<String, ValueDef> def : defs.entrySet()) {
            if(def.getValue().getIncluding().equals(Including.ONCE))
                if(m_vals.get(def.getKey()) == null)
                    throw new ENotNullableParam(def.getKey());
        }
    }

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    private void putValue(Map<String, ValueDef> defs, String name, Object value) {
        ValueDef def = defs.get(name);
        if(def == null)
            throw new EUnknownParam(name);
        if(!def.getType().isInstance(value))
            throw new EInvalidTypeParam(name);
        
        Object oldValue = m_vals.get(name);
        if(def.getIncluding().equals(Including.ARRAY)){
            if(oldValue == null){
                ArrayList<Object> vArr = new ArrayList<Object>();
                vArr.add(value);
                m_vals.put(name, vArr);
            }
            else{
                ((List<Object>)oldValue).add(value);
            }
        }
        else{
            if(oldValue != null)
                throw new EOnceIncludingParam(name);
            m_vals.put(name, value);
        }
    }

    public Object get(String name) {
        return m_vals.get(name);
    }

}
