/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.db.test.TFTestCase;

public class OlBExprContainsTest extends TFTestCase {
	@Override
	public void test() throws Exception {
        OlEval of = new OlEval();
        of.addExpr(0, new OlBExprContains("tHe"));
        assertFalse(of.evalArray(new Object[]{null}, 0));
        assertFalse(of.evalArray(new Object[]{1}, 0));
        assertFalse(of.evalArray(new Object[]{"2"}, 0));
        assertFalse(of.evalArray(new Object[]{"th"}, 0));
        assertTrue(of.evalArray(new Object[]{"the"}, 0));
        assertTrue(of.evalArray(new Object[]{"qqq the"}, 0));
        assertTrue(of.evalArray(new Object[]{"qqq ThE"}, 0));

	}
	
	public void testColumn(){
	    OlEval of = new OlEval();
        of.addExpr(0, new OlBExprContains( new OlExprColumn(1)));
        assertTrue(of.evalArray(new Object[]{"The", "The"}, 0));
        assertTrue(of.evalArray(new Object[]{"21", 1}, 0));
        assertTrue(of.evalArray(new Object[]{"21", 21}, 0));
        assertTrue(of.evalArray(new Object[]{"7682111", 21}, 0));
        assertTrue(of.evalArray(new Object[]{7682111, 21}, 0));
        assertFalse(of.evalArray(new Object[]{"21", 211}, 0));
        
        assertFalse(of.evalArray(new Object[]{null, 21}, 0));
        assertFalse(of.evalArray(new Object[]{"7682111", null}, 0));
        assertFalse(of.evalArray(new Object[]{null, null}, 0));
        
	}

}
