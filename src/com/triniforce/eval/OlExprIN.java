/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.utils.TFUtils;

public class OlExprIN extends OlExpr {
    final List<OlExprEQ> m_exprs = new ArrayList<OlExprEQ>();
            
    public OlExprIN(Object values[]) {
        TFUtils.assertNotNull(values, "IN values");
        for(Object value: values){
            m_exprs.add(new OlExprEQ(value));
        }
    }
    @Override
    public boolean evaluate(Object value) {
        for(OlExprEQ expr: m_exprs){
            if(expr.evaluate(value)) return true;
        }
        return false;
    }
}