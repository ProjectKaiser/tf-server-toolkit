/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

public class Ol_IdxExpr implements IOlEvaluator{
    private final int m_idx;
    private final OlExpr m_expr;
    public Ol_IdxExpr(int idx, OlExpr expr) {
        m_idx = idx;
        m_expr = expr;
    }
    public int getIdx() {
        return m_idx;
    }
    public OlExpr getExpr() {
        return m_expr;
    }
    public boolean evaluate(IOlValueGetter vg) {
        return getExpr().evaluate(vg.getValue(getIdx()));
    }
    
    @Override
    public String toString() {
        return null == m_expr?"null":m_expr.getClass().getSimpleName() + "(" + m_idx + ")";
    }
    
}