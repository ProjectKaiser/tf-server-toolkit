/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eval;

public abstract class OlExpr implements IOlExpr{

    public Object evalArray(final Object ...values) {
        IOlColumnGetter vg = new IOlColumnGetter() {
            
            public Object getValue(int idx) {
                          return values[idx];
            }
        };
        return eval(vg);
    }

}
