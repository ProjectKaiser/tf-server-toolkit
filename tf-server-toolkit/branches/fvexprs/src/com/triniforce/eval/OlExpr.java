/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

public abstract class OlExpr {
    public abstract boolean evaluate(Object columnValue, IOlColumnGetter vg);
    
    @Override
    public String toString() {
    	return getClass().getSimpleName();
    }
}