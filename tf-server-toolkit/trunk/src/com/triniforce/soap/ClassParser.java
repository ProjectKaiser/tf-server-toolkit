/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.utils.TFUtils;

public class ClassParser {
    
    private Package m_pkg;

    public ClassParser(Package pkg) {
        m_pkg = pkg;
    }

    public ClassDef parse(Class key, IDefLibrary lib, String typeName) {
        
        Class<?> cls = (Class) key;
        
        ClassDef parentDef = null;
        Class superclass = key.getSuperclass(); 
        if(null != superclass && (superclass.getPackage().equals(m_pkg) || superclass.getPackage().equals(cls.getPackage()))){
        	TypeDef def = lib.add(superclass);
        	if(def instanceof ClassDef)
        		parentDef = (ClassDef) def;
        	else{
        		//throw new ESoap.InvalidTypeName(typeName);
        	}
        }
        
        ClassDef res = new TypeDef.ClassDef(typeName, key, parentDef);
        
        Iterator<Method> setters = getSetters(cls);
        
        while(setters.hasNext()){
            Method setter = setters.next();
            String name = setter.getName().substring(3);
            String lowerName = Character.toString(Character.toLowerCase(name.charAt(0))) + name.substring(1);
            if(null != parentDef && null != parentDef.getProp(lowerName)){
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
                    Class propCls = TypeDefLibCache.toClass(propType);
                    TypeDef td = lib.add(propType);
                    PropDef propDef = new PropDef(lowerName, td, propCls.getName(), 
                            new ClassDef.CDGetSet(cls.getName(), getter.getName(), setter.getName()));
                    res.getOwnProps().add(propDef);
                }
//            } catch (SecurityException e) {
//            } catch (NoSuchMethodException e) {}
        }
        
        PropertiesSequence propSeq = (PropertiesSequence) key.getAnnotation(PropertiesSequence.class);
        if(null !=propSeq){
        	final String[]seq = propSeq.sequence();
	        Collections.sort(res.getOwnProps(), new Comparator<PropDef>(){
				public int compare(PropDef prop1, PropDef prop2) {
					return pos(prop1) - pos(prop2); 
				}
				int pos(PropDef prop){
					int res = Arrays.asList(seq).indexOf(prop.getName());
					return res < 0 ? seq.length : res;
				}
	        });
        }
        
//        for (Class innerCls : cls.getDeclaredClasses()) {
//        	int modifiers = innerCls.getModifiers();
//        	if(Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
//        		lib.add(innerCls);
//		}
        
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

        
}
