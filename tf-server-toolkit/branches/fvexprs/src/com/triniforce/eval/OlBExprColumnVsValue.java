/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eval;

/**
 * Compare column and test expression
 */
public abstract class OlBExprColumnVsValue  extends OlBExpr {

    private IOlExpr m_testExpr;
    boolean isTestExprConstant;
    Object m_workTestValue;
    
    public OlBExprColumnVsValue(Object testExpr) {
        m_testExpr = OlExprConstant.calcIExpr(testExpr);
    }

    public static Object calcWorkingTestValue(Object testValue, Object columnValue) {
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
    
    abstract boolean compareNotNullValues(Object columnValue, Object testValue);
        
    @Override
    public boolean eval(Object columnValue, IOlColumnGetter vg) {
        if (null == columnValue) {
            return null == getTestExpr().eval(vg);
        }
        if ( !m_testExpr.isConstant() ||  null == m_workTestValue) {
            m_workTestValue = calcWorkingTestValue(getTestExpr().eval(vg), columnValue);
        }
        if (null == m_workTestValue)
            return false;
        return compareNotNullValues(columnValue, m_workTestValue);
    }

    public IOlExpr getTestExpr() {
        return m_testExpr;
    }
    
    public abstract String getOpName();
    
    @Override
    public String toString() {
        return getOpName() + " " + getTestExpr().toString();
    }


}
