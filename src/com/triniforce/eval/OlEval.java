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
public class OlEval implements IOlEvaluator {

    List<OlEval> m_evals = new ArrayList<OlEval>();

    @Deprecated
    private final List<Ol_IdxExpr> m_idxExprs = new ArrayList<Ol_IdxExpr>();
    
    private final List<IOlEvaluator> m_evaluators = new ArrayList<IOlEvaluator>();
    private boolean m_andConcatenation = true;
    
    public void addExpr(int idx, OlExpr expr) {
        getIdxExprs().add(new Ol_IdxExpr(idx, expr));
    }
    
    public void addEval(OlEval eval) {
        m_evals.add(eval);
    }
    
    public boolean evaluate(IOlValueGetter vg){
        for (Ol_IdxExpr ie : getIdxExprs()) {
            if (ie.getExpr().evaluate(vg.getValue(ie.getIdx())) != m_andConcatenation){
                return !m_andConcatenation;
            }
        }
        for(OlEval eval: m_evals){
            if (eval.evaluate(vg) != m_andConcatenation){
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
        return evaluate(vg);
    }

    public boolean evalList(final List values, final int startIdx) {
        IOlValueGetter vg = new IOlValueGetter() {
            public Object getValue(int idx) {
                return values.get(startIdx + idx);
            }
        };
        return evaluate(vg);
    }

    public boolean isAndConcatenation() {
        return m_andConcatenation;
    }

    public void setAndConcatenation(boolean isAndConcatenation) {
        m_andConcatenation = isAndConcatenation;
    }

    @Deprecated
    public List<Ol_IdxExpr> getIdxExprs() {
        return m_idxExprs;
    }

    public List<IOlEvaluator> getEvaluators() {
        return m_evaluators;
    }

}
