/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.eval;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 1) Добавить несколько выражений через addExpr() 2) Выполнять testValues() по
 * необходимости
 * 
 */
public class OlEval {

    List<OlEval> m_evals = new ArrayList<OlEval>();

    private final List<IdxExpr> m_idxExprs = new ArrayList<IdxExpr>();
    private boolean m_isAndConcatenation = true;
    
    public void addExpr(int idx, OlExpr expr) {
        getIdxExprs().add(new IdxExpr(idx, expr));
    }
    
    public void addEval(OlEval eval) {
        m_evals.add(eval);
    }
    
    public boolean eval(IOlValueGetter vg){
        for (IdxExpr ie : getIdxExprs()) {
            if (ie.getExpr().evaluate(vg.getValue(ie.getIdx())) != m_isAndConcatenation){
                return !m_isAndConcatenation;
            }
        }
        for(OlEval eval: m_evals){
            if (eval.eval(vg) != m_isAndConcatenation){
                return !m_isAndConcatenation;
            }
        }
        return m_isAndConcatenation;
    }
    
    public boolean evalArray(final Object values[], final int startIdx){
        IOlValueGetter vg = new IOlValueGetter() {
            public Object getValue(int idx) {
                return values[startIdx + idx];
            }
        };
        return eval(vg);
    }

    public boolean evalList(final List values, final int startIdx) {
        IOlValueGetter vg = new IOlValueGetter() {
            public Object getValue(int idx) {
                return values.get(startIdx + idx);
            }
        };
        return eval(vg);
    }

    public boolean isIsAndConcatenation() {
        return m_isAndConcatenation;
    }

    public void setIsAndConcatenation(boolean isAndConcatenation) {
        m_isAndConcatenation = isAndConcatenation;
    }

    public List<IdxExpr> getIdxExprs() {
        return m_idxExprs;
    }
    

}
