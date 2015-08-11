/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eval;

import com.triniforce.db.test.TFTestCase;

public class OlBExprContainsIdTest extends TFTestCase {
	
	@Override
	public void test() throws Exception {
        OlEval of = new OlEval();
        of.addExpr(0, new OlBExprContainsId( new OlExprColumn(1)));
        assertFalse(of.evalArray(new Object[]{"1", null}, 0));
        assertTrue(of.evalArray(new Object[]{"1", "1"}, 0));
        assertFalse(of.evalArray(new Object[]{"2", "1"}, 0));
        
        assertTrue(of.evalArray(new Object[]{"2 1", "1"}, 0));
        
        assertFalse(of.evalArray(new Object[]{"2 1235", "123"}, 0));
        assertTrue(of.evalArray(new Object[]{"2 123 5", "123"}, 0));
        assertTrue(of.evalArray(new Object[]{"2 123 ", "123"}, 0));
        assertTrue(of.evalArray(new Object[]{"2 aa123", "aa123"}, 0));
        
        assertFalse(of.evalArray(new Object[]{null, "1"}, 0));
        assertFalse(of.evalArray(new Object[]{"2 1", null}, 0));
        assertFalse(of.evalArray(new Object[]{null, null}, 0));

	}

}
