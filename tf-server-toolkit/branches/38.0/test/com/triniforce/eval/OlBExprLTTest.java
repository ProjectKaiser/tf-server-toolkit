/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eval;

import com.triniforce.db.test.TFTestCase;

public class OlBExprLTTest extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        {
            OlEval ev = new OlEval();
            ev.addExpr(0, new OlBExprLT(2));
            assertFalse(ev.evalArray(new Object[]{2}, 0));
            assertTrue(ev.evalArray(new Object[]{1}, 0));
        }
        {
            OlEval ev = new OlEval();
            ev.addExpr(0, new OlBExprLT(2.3));
            assertFalse(ev.evalArray(new Object[]{2.3}, 0));
            assertTrue(ev.evalArray(new Object[]{2.2}, 0));
            trace(ev);
        }
    }

}
