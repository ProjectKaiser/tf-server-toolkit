/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

public abstract class OlExpr {
    public boolean evaluate(Object value) {
        return false;
    }
    
    @Override
    public String toString() {
    	return getClass().getSimpleName();
    }
}