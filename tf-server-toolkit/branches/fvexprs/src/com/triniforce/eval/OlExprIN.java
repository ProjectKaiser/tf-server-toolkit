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
    final List<OlExprEquals> m_exprs = new ArrayList<OlExprEquals>();
            
    public OlExprIN(Object values[]) {
        TFUtils.assertNotNull(values, "IN values");
        for(Object value: values){
            m_exprs.add(new OlExprEquals(value));
        }
    }
    @Override
    public boolean evaluate(Object value, IOlColumnGetter vg) {
        for(OlExprEquals expr: m_exprs){
            if(expr.evaluate(value, vg)) return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
		String res = super.toString() + "(";
		for (OlExprEquals e : m_exprs) {
			res += "," + e.getTestValue();
		}
		res += ")";
		return res;
    }
    
}