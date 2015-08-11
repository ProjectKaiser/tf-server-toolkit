/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.db.test.TFTestCase;

public class OlBExprEqualsTest extends TFTestCase {
	
	
	@Override
	public void test() throws Exception {
		OlEval ev = new OlEval();
		ev.addExpr(0,  new OlBExprEquals("The"));
		assertTrue(ev.evalArray(new Object[]{"The"}, 0));
	}

}
