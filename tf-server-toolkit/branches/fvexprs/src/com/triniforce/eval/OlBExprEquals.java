/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlBExprEquals extends OlBExpr {

    private final IOlExpr m_testExpr;

    Object m_workTestValue;

    public OlBExprEquals(Object testExpr) {
        m_testExpr = OlExprConstant.calcIExpr(testExpr);
    }

    public static Object getWorkingTestValue(Object testValue, Object columnValue) {
        if (null == testValue)
            return null;
        if (columnValue.getClass().equals(Long.class)) {
            return (Long) (((Number) testValue).longValue());
        }
        if (columnValue.getClass().equals(Integer.class)) {
            return (Integer) (((Number) testValue).intValue());
        }
        if (columnValue.getClass().equals(Short.class)) {
            return (Short) (((Number) testValue).shortValue());
        }
        return testValue;
    }

    boolean internal_evaluateValue(Object value) {
        return m_workTestValue.equals(value);
    }
    
    @Override
    public String toString() {
    	return " " + internal_getOpName() + " " + m_testExpr.toString();
    }

    String internal_getOpName() {
		return "=";
	}

	@Override
    public boolean eval(Object columnValue, IOlColumnGetter vg) {
        if (null == columnValue) {
            return null == getTestExpr();
        }
        if (null == m_workTestValue) {
            m_workTestValue = getWorkingTestValue(m_testExpr.eval(vg), columnValue);
        }
        if (null == m_workTestValue)
            return false;
        return internal_evaluateValue(columnValue);
    }

    public IOlExpr getTestExpr() {
        return m_testExpr;
    }
    
}