/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprNotNull extends OlBExpr {
    
    public OlBExprNotNull() {
    }
    
    @Override
    public Boolean eval(Object value, IOlColumnGetter vg) {
        return ( null != value);
    }
    
    @Override
    public String getOpName() {
        return "NOT NULL";
    }
}