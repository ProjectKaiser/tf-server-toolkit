/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.utils.TFUtils;

public class OlExprBetween extends OlExpr {
    private final OlExprGE m_leftExpr;
    private final OlExprLE m_rightExpr;
    public OlExprBetween(Object leftValue, Object rightValue) {
        TFUtils.assertNotNull(leftValue, "BETWEEN 'from' value");
        TFUtils.assertNotNull(rightValue, "BETWEEN 'to' value");            
        m_leftExpr = new OlExprGE(leftValue);
        m_rightExpr = new OlExprLE(rightValue);
    }
    @Override
    public boolean evaluate(Object value, IOlColumnGetter vg) {
        return getLeftExpr().evaluate(value, vg) && getRightExpr().evaluate(value, vg); 
    }
    public OlExprGE getLeftExpr() {
        return m_leftExpr;
    }
    public OlExprLE getRightExpr() {
        return m_rightExpr;
    }
}