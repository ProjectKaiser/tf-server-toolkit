/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;


public class OlExprEquals extends OlExpr {

    private final Object m_testValue;

    Object m_workTestValue;

    public OlExprEquals(Object testValue) {
        m_testValue = testValue;
    }

    public static Object getWorkingTestValue(Object testValue, Object value) {
        if (null == testValue)
            return null;
        if (value.getClass().equals(Long.class)) {
            return (Long) (((Number) testValue).longValue());
        }
        if (value.getClass().equals(Integer.class)) {
            return (Integer) (((Number) testValue).intValue());
        }
        if (value.getClass().equals(Short.class)) {
            return (Short) (((Number) testValue).shortValue());
        }
        return testValue;
    }

    boolean evaluateValue(Object value) {
        return m_workTestValue.equals(value);
    }
    
    @Override
    public String toString() {
    	return super.toString() +"(" +m_testValue +")";
    }

    @Override
    public boolean evaluate(Object columnValue, IOlColumnGetter vg) {
        if (null == columnValue) {
            return columnValue == getTestValue();
        }
        if (null == m_workTestValue) {
            m_workTestValue = getWorkingTestValue(getTestValue(), columnValue);
        }
        if (null == m_workTestValue)
            return false;
        return evaluateValue(columnValue);
    }

    public Object getTestValue() {
        return m_testValue;
    }
    
    
    
}