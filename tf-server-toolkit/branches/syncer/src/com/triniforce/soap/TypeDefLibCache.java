/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.soap.IDefLibrary.ITypeNameGenerator;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.MapDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiAlgs.SimpleName;
import com.triniforce.utils.UniqueNameGenerator;

public class TypeDefLibCache implements IDefLibrary, ITypeNameGenerator{
    
    static class ScalarDefLib implements IDefLibrary{
        HashMap<Class, ScalarDef> m_scalars;
        Class SCALAR_TYPES[] = { 
                Boolean.class, Boolean.TYPE, 
                Integer.class, Integer.TYPE, 
                Long.class, Long.TYPE,
                Short.class, Short.TYPE, 
                Float.class, Float.TYPE, 
                Double.class, Double.TYPE, 
                String.class, Object.class,
                Date.class, BigDecimal.class, 
                byte[].class
        };
        ScalarDefLib(){
            m_scalars = new HashMap<Class, ScalarDef>();
            for (Class scType : SCALAR_TYPES) {
                m_scalars.put(scType, new ScalarDef(scType));
            }
        }
        
        public ScalarDef add(Type type) {
            return get(type);
        }

        public ScalarDef get(Type type) {
        	Class cls = TypeDefLibCache.toClass(type);
            return m_scalars.get(cls);
        }
        
        public Collection<ScalarDef> getDefs() {
            return m_scalars.values();
        }
        
    }
    
    static final ScalarDefLib SCALAR_LIB = new ScalarDefLib();
    
    static class ClassDefLib implements IDefLibrary{
        
        Map<Type, TypeDef> m_cache;
        private ClassParser m_parser;
        private IDefLibrary m_parent;
		private ITypeNameGenerator m_nameGen;

        public ClassDefLib(ClassParser parser, IDefLibrary parent, 
        		Map<Type, TypeDef> m_classes, ITypeNameGenerator gen) {
            m_parser = parser;
            m_parent = parent;
            m_cache = m_classes;
            m_nameGen = gen;
        }
        
        public TypeDef add(Type type) {
            TypeDef res = get(type);
            if(null == res){
            	Class cls = toClass(type);
            	String name = m_nameGen.get(type, cls.getSimpleName(), true);
				if(cls.isEnum())
					res = new TypeDef.EnumDef(name, (Class) type);
				else{
	                res = m_parser.parse(cls, m_parent, name);
				}
                m_cache.put(type, res);
                
                for (Class innerCls : cls.getDeclaredClasses()) {
                	int modifiers = innerCls.getModifiers();
                	if(Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
                		m_parent.add(innerCls);
        		}

            }
            return res;
        }

        public TypeDef get(Type type) {
            return m_cache.get(type);
        }
//
//        public Collection<ClassDef> getDefs() {
//            return m_cache.values();
//        }
        
    }
    
    static class ArrayDefLib implements IDefLibrary{
        
        private IDefLibrary m_parent;
        Map<TypeDef, ArrayDef> m_arrays;
		private ITypeNameGenerator m_nameGen;
        public ArrayDefLib(IDefLibrary parent, Map<TypeDef, ArrayDef> cache, ITypeNameGenerator gen) {
            m_parent = parent;
            m_arrays = cache;
            m_nameGen = gen;
        }

        public ArrayDef add(Type type) {
            KeyRawType key = type2key(type, true);
            ArrayDef res=null;
            if(null != key){
                res = m_arrays.get(key.m_key);
                if(null == res){
                    String  compName = "ArrayOf" + Character.toUpperCase(key.m_key.getName().charAt(0)) + key.m_key.getName().substring(1);
                    compName = m_nameGen.get(type, compName, false);
                    res = new ArrayDef(compName, key.m_rawType, key.m_key);
                    m_arrays.put(key.m_key, res);
                }
            }
            return res;
        }
        
        static class KeyRawType{
            Class m_rawType;
            TypeDef m_key;
        }
        
        KeyRawType type2key(Type type, boolean bAddKey){
            Type componentType = null; 
            Class rawType = null;
            
            if(type instanceof Class){
                Class cls = (Class) type;
                if(cls.isArray()){
                    rawType = cls;
                    componentType = cls.getComponentType();
                }
            }
            else{
                if(type instanceof ParameterizedType){
                    final ParameterizedType pt = (ParameterizedType) type;
                    if(List.class.equals(pt.getRawType())){
                        componentType = toClass(pt.getActualTypeArguments()[0]);
                        rawType = Array.newInstance((Class<?>) componentType, 0).getClass();
                    	componentType = pt.getActualTypeArguments()[0];
                    }
                }
                else if(type instanceof GenericArrayType){
                    GenericArrayType ga = (GenericArrayType) type;
                    componentType = ga.getGenericComponentType();
                    rawType = Array.newInstance((Class<?>) ga.getGenericComponentType(), 0).getClass();
                }
            }
            KeyRawType res=null;
            if(null != componentType){
                res = new KeyRawType();
                res.m_rawType = rawType;
                res.m_key = bAddKey ? m_parent.add(componentType) : m_parent.get(componentType); 
            }
            return res;
        }
        
        public ArrayDef get(Type type) {
            KeyRawType key = type2key(type, false);
            ArrayDef res=null;
            if(null != key){
                res = m_arrays.get(key.m_key);
            }
            return res;
        }

        public Collection<ArrayDef> getDefs() {
            return m_arrays.values();
        } 
    }
    
    static class MapDefLib implements IDefLibrary{

        private IDefLibrary m_parent;
        private Map<TypeDef, ArrayDef> m_maps;
		private ITypeNameGenerator m_nameGen;
        public MapDefLib(IDefLibrary parent, Map<TypeDef, ArrayDef> m_arrays, ITypeNameGenerator gen) {
            m_parent = parent;
            m_maps   = m_arrays;
            m_nameGen = gen;
        }
        
        static class MapComponentDef extends ClassDef{
            private static final long serialVersionUID = -7007326289948350308L;
            public MapComponentDef(TypeDef keyDef, TypeDef valDef) {
                super(entryName(keyDef, valDef), MapEntry.class);
                try {
                    getOwnProps().add(new PropDef("key", keyDef, keyDef.getType(),
                            new ClassDef.CDGetSet(Map.Entry.class.getName(), "getKey", MapEntry.class.getName(), "setKey")));
                    getOwnProps().add(new PropDef("value", valDef, valDef.getType(),
                            new ClassDef.CDGetSet(Map.Entry.class.getName(), "getValue", "setValue")));
                } catch (Exception e) {
                    ApiAlgs.rethrowException(e);
                }
            }
            
            @Override
            public boolean equals(Object arg0) {
                if(!(arg0 instanceof MapComponentDef))
                    return false;
                MapComponentDef other = (MapComponentDef)arg0;
                return 
                    getKeyDef().equals(other.getKeyDef()) &&
                    getValueDef().equals(other.getValueDef());
            }
            
            @Override
            public int hashCode() {
                return getKeyDef().hashCode() + getValueDef().hashCode();
            }
            
            TypeDef getKeyDef(){
                return getProps().get(0).getType();
            }
            TypeDef getValueDef(){
                return getProps().get(1).getType();
            }
        }
        
        public ArrayDef add(Type type) {
            ArrayDef res = null;
            MapComponentDef componentDef = type2key(type, true);
            
            if(null != componentDef){
                res = m_maps.get(componentDef);
                if(null == res){
                	String name = mapName(componentDef.getKeyDef(), componentDef.getValueDef());
                	name = m_nameGen.get(type, name, false);
                    res = new MapDef(name, componentDef);
                    m_maps.put(componentDef, res);
                }
            }
            return res;
        }

        private static String mapName(TypeDef keyDef, TypeDef valDef) {
            String res = "MapOf"+upperName(valDef.getName())+"By"+upperName(keyDef.getName());
            return res;
        }
        
        private static String entryName(TypeDef keyDef, TypeDef valDef) {
            String res = "MapEntry"+upperName(valDef.getName())+"By"+upperName(keyDef.getName());
            return res;
        }
        
        static String upperName(String name){
            String res = "";
            res += Character.toUpperCase(name.charAt(0));
            res += name.substring(1);
            return res;
        }

        public ArrayDef get(Type type) {
            TypeDef componentDef = type2key(type, false);
            ArrayDef res=null;
            if(null != componentDef){
                res = m_maps.get(componentDef);
            }
            return res;
        }
        
        MapComponentDef type2key(Type type, boolean bAddKey){
            Type tkey=null, tval=null;
            if(type instanceof ParameterizedType){
                ParameterizedType pt = (ParameterizedType) type;
                if(pt.getRawType().equals(Map.class)){
                    Type[] params = pt.getActualTypeArguments();
                    tkey = params[0];
                    tval = params[1];
                }
            }
            else if(type.equals(Map.class)){
                tkey= Object.class; 
                tval = Object.class;
            }
            
            if(null == tkey)
                return null;
            
            return bAddKey ? new MapComponentDef(m_parent.add(tkey), m_parent.add(tval)) :
                new MapComponentDef(m_parent.get(tkey), m_parent.get(tval));
        }
    }
        
//    static class EnumDefLib implements IDefLibrary{
//
//		public TypeDef add(Type type) {
//			return null;
//		}
//
//		public TypeDef get(Type type) {
//			return null;
//		}
//    	
//    }
    
    public static class PropDef extends SimpleName implements Serializable{
        private static final long serialVersionUID = -2146710337654704756L;
        private transient TypeDef m_type;
        private String m_rawTypeName;
        private IGetSet m_getSet;
        
        interface IGetSet{
            Object get(Object obj);
            void set(Object obj, Object value);
        }
        
        public PropDef() {
            super(null);
        }
        
        public PropDef(String name, TypeDef type, String rawType, IGetSet getSet) {
            super(name);
            m_type = type;
            m_rawTypeName  = rawType;
            m_getSet = getSet;
        }
        public TypeDef getType() {
            return m_type;
        }
        
        public String getRawType() {
            return m_rawTypeName;
        }
        
        public void set(Object obj, Object value) {
            value = toRawValue(value);
            m_getSet.set(obj, value);
        }
        protected Object toRawValue(Object value) {
            try {
            	TypeDef type = getType();
                if(type instanceof ArrayDef){
                    ArrayDef ad = (ArrayDef) type;
                    Class<?> cls = Class.forName(ad.getPropDef().getRawType());
                    Class<?> rawCls = Class.forName(getRawType());
                    if(rawCls.isArray() && cls.isArray()){
                    	Class<?> compType = cls.getComponentType();
                        value = convertListToArray(compType, (List) value);
                    }
                }
            } catch (ClassNotFoundException e) {
                ApiAlgs.rethrowException(e);
            }
            return value;
        }
        private Object convertListToArray(Class compType, List value) {
            Object res = Array.newInstance(compType, value.size());
            int i=0;
            for (Object object : value) {
            	if(compType.equals(int.class))
            		Array.setInt(res, i, ((Integer)object).intValue());
            	else if(compType.equals(byte.class))
	            	Array.setByte(res, i, ((Byte)object).byteValue()); 
            	else if(compType.equals(char.class)) 
            		Array.setChar(res, i, ((Character)object).charValue());
            	else if(compType.equals(double.class))
            		Array.setDouble(res, i, ((Double)object).doubleValue());
            	else if(compType.equals(float.class))
            		Array.setFloat(res, i, ((Float)object).floatValue());
            	else if(compType.equals(long.class))
            		Array.setLong(res, i, ((Long)object).longValue());
            	else if(compType.equals(short.class))
            		Array.setShort(res, i, ((Short)object).shortValue());
            	else
            		Array.set(res, i, object);
            	i++;
            }
            return res;
        }

        public Object get(Object obj) {
            return m_getSet.get(obj);
        }
    }
    
    Map<TypeDef, ArrayDef> m_arrays;
    List<IDefLibrary> m_libs;
    Map<Type, TypeDef> m_classes;
	private UniqueNameGenerator m_uniqueNameGen;
	private ExternalDefLib m_extLib;

    public TypeDefLibCache(ClassParser parser) {
        m_libs = new ArrayList<IDefLibrary>();
        m_arrays = new HashMap<TypeDef, ArrayDef>();
        m_classes = new HashMap<Type, TypeDef>();
        m_extLib = new ExternalDefLib();
        m_libs.add(new ScalarDefLib());
        m_libs.add(new ArrayDefLib(this, m_arrays, this));
        m_libs.add(new MapDefLib(this, m_arrays, this));
        m_libs.add(m_extLib);
        m_libs.add(new ClassDefLib(parser, this, m_classes, this));
        m_uniqueNameGen = new UniqueNameGenerator();
    }
    
    @PropertiesSequence(sequence = {"key","value"})
    static class MapEntry implements Map.Entry{

        private Object m_key;
        private Object m_value;

        public Object getKey() {
            return m_key;
        }

        public Object getValue() {
            return m_value;
        }

        public Object setValue(Object arg0) {
            Object res = m_value;
            m_value = arg0;
            return res;
        }
        
        public void setKey(Object arg0) {
            m_key= arg0;
        }
        
    }

    public TypeDef add(Type type) {
        TypeDef res = null;
        for (IDefLibrary lib : m_libs) {
            res = lib.add(type);
            if(null != res)
                break;
        }
        return res;
    }

    public TypeDef get(Type type) {
        TypeDef res = null;
        for (IDefLibrary lib : m_libs) {
            res = lib.get(type);
            if(null != res)
                break;
        }
        return res;   
    }

    public List<TypeDef> getDefs() {
        ArrayList<TypeDef> res = new ArrayList<TypeDef>(m_arrays.values());
        res.addAll(m_classes.values());
        res.addAll(m_extLib.getDefs());
        for(ArrayDef ad : m_arrays.values()){
        	if(ad instanceof MapDef){
        		MapDef md = (MapDef) ad;
        		res.add(md.getComponentType());
        	}
        }
        return res;
    }
    
    public static Class toClass(Type type) {
        // GenericArrayType, ParameterizedType, TypeVariable<D>, WildcardType
        if (type instanceof Class) {
            Class clazz = (Class) type;
            return clazz;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Class componentType = toClass(arrayType.getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return toClass(parameterizedType.getRawType());
        } else {
            return Object.class;
        }
    }
    
    public String get(Type type, String template, boolean bThrowIfExists){
    	if(bThrowIfExists && m_uniqueNameGen.contains(template)){
    		throw new ESoap.InvalidTypeName(template);
    	}
    	return m_uniqueNameGen.generate(template);
    }

}