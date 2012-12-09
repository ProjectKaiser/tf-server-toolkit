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

	
	private boolean m_not;

    private final List<IOlEvaluator> m_evaluators = new ArrayList<IOlEvaluator>();
    private boolean m_andConcatenation = true;
    
    public void addExpr(int idx, OlExpr expr) {
        getEvaluators().add(new Ol_IdxExpr(idx, expr));
    }
    
    public void addEval(OlEval eval) {
        getEvaluators().add(eval);
    }
    
    public boolean evaluate(IOlValueGetter vg){
        for (IOlEvaluator e : getEvaluators()) {
            if (e.evaluate(vg) != m_andConcatenation){
                return isNot() ^ !m_andConcatenation;
            }
        }
        return isNot() ^  m_andConcatenation;
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

    public List<IOlEvaluator> getEvaluators() {
        return m_evaluators;
    }

	public boolean isNot() {
		return m_not;
	}

	public void setNot(boolean not) {
		m_not = not;
	}

}
