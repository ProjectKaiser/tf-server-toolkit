/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

/**
 * Boolean expression
 *
 */
public abstract class OlBExpr {
    public abstract boolean eval(Object columnValue, IOlColumnGetter vg);
    
    @Override
    public String toString() {
    	return " " + getClass().getSimpleName();
    }
    
    public static String safeToString(OlBExpr bexpr){
    	if(null == bexpr){
    		return " nullOp";
    	}
    	return bexpr.toString();
    }
    
}