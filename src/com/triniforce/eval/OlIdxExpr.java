/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

public class OlIdxExpr{
    private final int m_idx;
    private final OlExpr m_expr;
    public OlIdxExpr(int idx, OlExpr expr) {
        m_idx = idx;
        m_expr = expr;
    }
    public int getIdx() {
        return m_idx;
    }
    public OlExpr getExpr() {
        return m_expr;
    }
}