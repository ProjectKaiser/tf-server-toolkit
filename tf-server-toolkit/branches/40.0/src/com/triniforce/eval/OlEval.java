/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.eval;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.utils.TFUtils;


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
    
    public void addExpr(int idx, OlBExpr expr) {
        getEvaluators().add(new Ol_IdxExpr(idx, expr));
    }
    
    public void addEval(IOlEvaluator eval) {
        getEvaluators().add(eval);
    }
    
    public boolean evaluate(IOlColumnGetter vg){
        return TFUtils.equals(true, evaluateThreeValued(vg));
    }
    
    public Boolean evaluateThreeValued(IOlColumnGetter vg){
        for (IOlEvaluator e : getEvaluators()) {
            Boolean evRes = e.evaluateThreeValued(vg);
            if(null == evRes) {
                return null;
            }
            if (evRes != m_andConcatenation){
                return isNot() ^ !m_andConcatenation;
            }
        }
        return isNot() ^  m_andConcatenation;
    }
    
    public boolean evalArray(final Object values[], final int startIdx){
        IOlColumnGetter vg = new IOlColumnGetter() {
            public Object getValue(int idx) {
                return values[startIdx + idx];
            }
        };
        return evaluate(vg);
    }

    public boolean evalList(final List values, final int startIdx) {
        IOlColumnGetter vg = new IOlColumnGetter() {
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
	
	@Override
	public String toString() {
		String res = isNot()?"not (":"(";
		for(IOlEvaluator e: m_evaluators){
			res += isAndConcatenation()?" and ":" or ";
			res +=e.toString();
		}
		res+=")";
		return res;
	}
}
