/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprNotNull extends OlBExpr {
    @Override
    public boolean evaluate(Object value, IOlColumnGetter vg) {
        return ( null != value);
    }
}