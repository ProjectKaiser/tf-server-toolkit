/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.utils.TFUtils;

public class OlBExprIN extends OlBExpr {
    final List<OlBExprEquals> m_exprs = new ArrayList<OlBExprEquals>();
            
    public OlBExprIN(Object values[]) {
        TFUtils.assertNotNull(values, "IN values");
        for(Object value: values){
            m_exprs.add(new OlBExprEquals(value));
        }
    }
    @Override
    public boolean evaluate(Object value, IOlColumnGetter vg) {
        for(OlBExprEquals expr: m_exprs){
            if(expr.evaluate(value, vg)) return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
		String res = super.toString() + "(";
		for (OlBExprEquals e : m_exprs) {
			res += "," + e.getTestValue();
		}
		res += ")";
		return res;
    }
    
}