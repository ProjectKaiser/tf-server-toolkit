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
    Object m_comparableConstantTestValue;
    boolean isTestValueCalculated = false;
    
    public OlBExprColumnVsValue(Object testExpr) {
        m_testExpr = OlExprConstant.calcIExpr(testExpr);
    }

    public static Object calcComparableTestValue(Object testValue, Object columnValue) {
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
        if (columnValue.getClass().equals(String.class)) {
            return testValue.toString().toLowerCase();
        }
        return testValue;
    }
    
    abstract boolean compareNotNullValues(Object columnValue, Object testValue);
    
    Boolean bothNulls(){
        return null;
    }
        
    @Override
    public Boolean eval(Object columnValue, IOlColumnGetter vg) {
        if (null == columnValue) {
            if(null == getTestExpr().eval(vg)){
                return bothNulls();
            }
            return null;
        }
        if ( !m_testExpr.isConstant() ||  !isTestValueCalculated) {
            m_comparableConstantTestValue = calcComparableTestValue(getTestExpr().eval(vg), columnValue);
            isTestValueCalculated = true;
        }
        if (null == m_comparableConstantTestValue)
            return null;
        return compareNotNullValues(columnValue, m_comparableConstantTestValue);
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
