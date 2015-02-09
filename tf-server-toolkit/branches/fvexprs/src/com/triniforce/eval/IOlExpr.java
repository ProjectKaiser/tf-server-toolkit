/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

public interface IOlExpr {
	Object eval(IOlColumnGetter vg);
	boolean isConstant();
}
