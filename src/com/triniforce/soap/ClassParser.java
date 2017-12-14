/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.TypeDefLibCache.PropDef.IGetSet;
import com.triniforce.utils.TFUtils;

public class ClassParser {
    
    private static final Comparator<PropDef> ALPHABETICAL_COMPARATOR = new Comparator<PropDef>(){

		@Override
		public int compare(PropDef o1, PropDef o2) {
			return o1.getName().compareTo(o2.getName());
		}
    	
    };
	private Package m_pkg;
	private HashMap<Class, List<String> > m_nonParsedParents;
	private List<CustomSerializer<?,?>> m_customSrzs;

    public ClassParser(Package pkg, List<CustomSerializer<?,?>> customSrzs) {
        m_pkg = pkg;
        m_nonParsedParents = new HashMap<Class, List<String> >();
        addNonParsedParent(Exception.class);
        m_customSrzs = customSrzs;
    }

    public ClassDef parse(Class key, IDefLibrary lib, String typeName) {
        
        Class<?> cls = (Class) key;
        
        ClassDef parentDef = null;
        Class superclass = key.getSuperclass(); 
        List<String> parentProperties = Collections.EMPTY_LIST;
        if(m_nonParsedParents.containsKey(superclass)){
        	parentProperties = m_nonParsedParents.get(superclass);
        	if(null == parentProperties){
        		parentProperties = extractClassProperties(superclass);
        		m_nonParsedParents.put(superclass, parentProperties);
        	}
        }
        else{
	        if(null != superclass && (
	        		superclass.getPackage().equals(m_pkg) || 
	        		superclass.getPackage().equals(cls.getPackage()) ||
	        		null != lib.get(superclass))){
	        	TypeDef def = lib.add(superclass);
	        	if(def instanceof ClassDef){
	        		parentDef = (ClassDef) def;
	        		parentProperties = extractClassProperties(superclass);
	        	}
	        	else{
	        		//throw new ESoap.InvalidTypeName(typeName);
	        	}
	        }
        }
        
        ClassDef res = new TypeDef.ClassDef(typeName, key, parentDef);
        
        Iterator<Method> setters = getSetters(cls);
        
        while(setters.hasNext()){
            Method setter = setters.next();
            String name = setter.getName().substring(3);
            String lowerName = Character.toString(Character.toLowerCase(name.charAt(0))) + name.substring(1);
            if(parentProperties.contains(lowerName)){
                // property from parent
                continue;
            }
            Type propType = setter.getGenericParameterTypes()[0];
            Method getter = null;
//            try {
                getter = getGetter(cls, name, propType);
                if(null != getter){
//                if(!Modifier.isStatic(getter.getModifiers()) && 
//                        propType.equals(getter.getGenericReturnType())){
                	CustomSerializer customSrz= null;
                	if(propType instanceof Class)
                		customSrz = CustomSerializer.find(m_customSrzs, (Class) propType);
                	IGetSet getset;
					if(null != customSrz){
                		getset = customSrz.getGetSet(getter, setter);
                		propType = customSrz.getTargetType();
                	}
                	else{
                		getset = new ClassDef.CDGetSet(cls.getName(), getter.getName(), setter.getName());
                	}
                    Class propCls = TypeDefLibCache.toClass(propType);
                    TypeDef td = lib.add(propType);
                    PropDef propDef = new PropDef(lowerName, td, propCls.getName(), getset);
                    res.getOwnProps().add(propDef);
                }
//            } catch (SecurityException e) {
//            } catch (NoSuchMethodException e) {}
        }
        
        PropertiesSequence propSeq = (PropertiesSequence) key.getAnnotation(PropertiesSequence.class);
        Comparator<PropDef> comparator;
        if(null !=propSeq){
        	final String[]seq = propSeq.sequence();
	        comparator =  new Comparator<PropDef>(){
				public int compare(PropDef prop1, PropDef prop2) {
					return pos(prop1) - pos(prop2); 
				}
				int pos(PropDef prop){
					int res = Arrays.asList(seq).indexOf(prop.getName());
					return res < 0 ? seq.length : res;
				}
	        };
        }else{
        	comparator = ALPHABETICAL_COMPARATOR;
        }
        Collections.sort(res.getOwnProps(), comparator);
        
//        for (Class innerCls : cls.getDeclaredClasses()) {
//        	int modifiers = innerCls.getModifiers();
//        	if(Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
//        		lib.add(innerCls);
//		}
        
        return res;
    }   

	private List<String> extractClassProperties(Class cls){
        Iterator<Method> setters = getSetters(cls);

        ArrayList<String> res = new ArrayList<String>();
        while(setters.hasNext()){
            Method setter = setters.next();
            String name = setter.getName().substring(3);
            String lowerName = Character.toString(Character.toLowerCase(name.charAt(0))) + name.substring(1);

            Type propType = setter.getGenericParameterTypes()[0];
            Method getter = null;
            getter = getGetter(cls, name, propType);
            if(null != getter){
            	res.add(lowerName);
            }
        }
    	return res;
    }

    private Method getGetter(Class cls, String name, Type type){
    	Method res=null;
    	if(boolean.class.equals(type) || Boolean.class.equals(type)){
    		res = getGetterByName(cls, "is"+name, type);

    	}
    	if(null == res){
    		res = getGetterByName(cls, "get"+name, type);
    	}
		return res;
	}

	private Method getGetterByName(Class cls, String getterName, Type type) {
		Method res = null;
		try {
			res = cls.getMethod(getterName, (Class[])null);
			if(Modifier.isStatic(res.getModifiers()) || 
                    (!type.equals(res.getGenericReturnType()))){
				res = null;
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} 
		return res;
	}

	Iterator<Method> getSetters(Class<?> cls) {
        final Method[] methods = cls.getMethods();
        return new Iterator<Method>(){
            int iMethod;
            {
                iMethod = 0;
                findNext();
            }
            public boolean hasNext() {
                return iMethod < methods.length;
            }
            public Method next() {
                Method res = null;
                if(hasNext()){
                    res = methods[iMethod];
                    iMethod++ ;
                    findNext();
                }
                return res;
            }
            private void findNext() {
                while(iMethod < methods.length) {
                    Method res = methods[iMethod];
                    if(isSetter(res))
                        break;
                    iMethod++;
                }
            }
            
            public void remove() {
                TFUtils.assertTrue(false, "unimplemented");
            }
        };
    }

    boolean isSetter(Method m){
        return 
            m.getName().startsWith("set") &&
            !Modifier.isStatic(m.getModifiers()) && 
            m.getName().length() > 3 &&
            m.getGenericParameterTypes().length == 1;
    }

	public void addNonParsedParent(Class class1) {
		m_nonParsedParents.put(class1, null);
	}

        
}
