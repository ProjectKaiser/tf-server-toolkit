/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class Ol_IdxExpr implements IOlEvaluator{
    private final int m_idx;
    private final OlBExpr m_expr;
    public Ol_IdxExpr(int idx, OlBExpr expr) {
        m_idx = idx;
        m_expr = expr;
    }
    public int getIdx() {
        return m_idx;
    }
    public OlBExpr getExpr() {
        return m_expr;
    }
    public boolean evaluate(IOlColumnGetter vg) {
        return getExpr().eval(vg.getValue(getIdx()), vg);
    }
    
    @Override
    public String toString() {
        return null == m_expr?"null":"col[" + m_idx + "] " + m_expr.toString();
    }
    
}