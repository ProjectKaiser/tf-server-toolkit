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

    private final List<OlIdxExpr> m_idxExprs = new ArrayList<OlIdxExpr>();
    private boolean m_andConcatenation = true;
    
    public void addExpr(int idx, OlExpr expr) {
        getIdxExprs().add(new OlIdxExpr(idx, expr));
    }
    
    public void addEval(OlEval eval) {
        m_evals.add(eval);
    }
    
    public boolean eval(IOlValueGetter vg){
        for (OlIdxExpr ie : getIdxExprs()) {
            if (ie.getExpr().evaluate(vg.getValue(ie.getIdx())) != m_andConcatenation){
                return !m_andConcatenation;
            }
        }
        for(OlEval eval: m_evals){
            if (eval.eval(vg) != m_andConcatenation){
                return !m_andConcatenation;
            }
        }
        return m_andConcatenation;
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

    public boolean isAndConcatenation() {
        return m_andConcatenation;
    }

    public void setAndConcatenation(boolean isAndConcatenation) {
        m_andConcatenation = isAndConcatenation;
    }

    public List<OlIdxExpr> getIdxExprs() {
        return m_idxExprs;
    }
    

}
