/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.EUtils.EAssertNotNullFailed;

public class OlExprContainsTest extends TFTestCase {
	@Override
	public void test() throws Exception {
        try {
            new OlExprContains(null);
            fail();
        } catch (EAssertNotNullFailed e) {
            trace(e);
        }
        
        OlEval of = new OlEval();
        of.addExpr(0, new OlExprContains("tHe"));
        assertFalse(of.evalArray(new Object[]{null}, 0));
        assertFalse(of.evalArray(new Object[]{1}, 0));
        assertFalse(of.evalArray(new Object[]{"2"}, 0));
        assertFalse(of.evalArray(new Object[]{"th"}, 0));
        assertTrue(of.evalArray(new Object[]{"the"}, 0));
        assertTrue(of.evalArray(new Object[]{"qqq the"}, 0));
        assertTrue(of.evalArray(new Object[]{"qqq ThE"}, 0));

	}

}
