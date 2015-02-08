/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlExprNotNull extends OlExpr {
    @Override
    public boolean evaluate(Object value, IOlColumnGetter vg) {
        return ( null != value);
    }
}