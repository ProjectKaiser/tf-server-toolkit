/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.utils.TFUtils;

public class OlBExprBetween extends OlBExpr {
    private final OlBExprGE m_leftExpr;
    private final OlBExprLE m_rightExpr;
    public OlBExprBetween(Object leftValue, Object rightValue) {
        TFUtils.assertNotNull(leftValue, "BETWEEN 'from' value");
        TFUtils.assertNotNull(rightValue, "BETWEEN 'to' value");            
        m_leftExpr = new OlBExprGE(leftValue);
        m_rightExpr = new OlBExprLE(rightValue);
    }
    @Override
    public boolean evaluate(Object value, IOlColumnGetter vg) {
        return getLeftExpr().evaluate(value, vg) && getRightExpr().evaluate(value, vg); 
    }
    public OlBExprGE getLeftExpr() {
        return m_leftExpr;
    }
    public OlBExprLE getRightExpr() {
        return m_rightExpr;
    }
}