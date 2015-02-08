/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

public class OlExprConstant implements IOlExpr{

	private final Object m_constValue;

	public OlExprConstant(Object constValue){
		m_constValue = constValue;
	}
	
	public Object eval(IOlColumnGetter vg) {
		return m_constValue;
	}
	
	public static IOlExpr calcIExpr(Object value){
		if(value instanceof IOlExpr){
			return (IOlExpr) value;
		}
		return new OlExprConstant(value);
	}
	
	@Override
	public String toString() {
		if(null == m_constValue){
			return "null";
		}
		return m_constValue.toString();
	}
	
}
