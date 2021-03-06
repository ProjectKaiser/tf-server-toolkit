/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.triniforce.soap.TypeDefLibCache.PropDef.IGetSet;
import com.triniforce.utils.ApiAlgs;

public abstract class CustomSerializer<From, To>{
	private Class<From> m_fromCls;
	private Class<To> m_toCls;

	public CustomSerializer(Class<From> fromCls, Class<To> toCls) {
		m_fromCls = fromCls;
		m_toCls = toCls;
	}
	
	public abstract To serialize(From value);
	public abstract From deserialize(To value);
	
	IGetSet getGetSet(final Method getter, final Method setter){
		return getGetSet(new IGetSet(){

			@Override
			public Object get(Object obj) {
				try {
					return getter.invoke(obj);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ApiAlgs.RethrownException(e);
				}
			}

			@Override
			public void set(Object obj, Object value) {
				try{
					setter.invoke(obj, value);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ApiAlgs.RethrownException(e);
				}
			}
			
		});
		
	}
	
	IGetSet getGetSet(final IGetSet fromGS){
		return new IGetSet(){
			@Override
			public Object get(Object obj) {
				From objt;
				objt = (From)fromGS.get(obj);
				return serialize( objt);
			}

			@Override
			public void set(Object obj, Object value) {
				From objt = deserialize((To) value);
				fromGS.set(obj, objt);
			}
			
		};
		
	}
	
	public Class<To> getTargetType(){
		return m_toCls;
	}

	public Class<From> getSourceType() {
		return m_fromCls;
	}

	public static CustomSerializer<?,?> find(List<CustomSerializer<?,?>> customSerializers, Type argType) {
		Class argCls = TypeDefLibCache.toClass(argType);
		for (CustomSerializer srz : customSerializers) {
			if(srz.getSourceType().isAssignableFrom(argCls))
				return srz;
		}
		return null;
	}

}
