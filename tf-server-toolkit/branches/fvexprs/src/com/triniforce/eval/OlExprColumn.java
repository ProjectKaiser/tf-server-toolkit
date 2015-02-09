/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import com.triniforce.utils.TFUtils;

public class OlExprColumn  implements IOlExpr{

	private final int m_idx;

	public OlExprColumn(int idx){
		TFUtils.assertTrue(idx >= 0, "idx < 0:" + idx);
		m_idx = idx;
	}
	
	public Object eval(IOlColumnGetter vg) {
		return vg.getValue(m_idx);
	}
	
	@Override
	public String toString() {
		return "col[" + m_idx + "]";
	}
	
	public boolean isConstant() {
	    return false;
	}

}
