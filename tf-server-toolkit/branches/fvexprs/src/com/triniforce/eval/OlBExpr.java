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
    public abstract boolean evaluate(Object columnValue, IOlColumnGetter vg);
    
    @Override
    public String toString() {
    	return getClass().getSimpleName();
    }
}