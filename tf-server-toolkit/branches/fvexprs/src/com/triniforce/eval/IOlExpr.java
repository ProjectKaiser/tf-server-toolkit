/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

public interface IOlExpr {
	Object eval(IOlColumnGetter vg);
	Object evalArray(Object ...value);
	boolean isConstant();
}
