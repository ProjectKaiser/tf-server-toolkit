/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.List;

import com.triniforce.eval.IOlColumnGetter;
import com.triniforce.eval.OlEval;
import com.triniforce.eval.OlExprEquals;
import com.triniforce.server.plugins.kernel.outline.OlEvalCVRConvertor;
import com.triniforce.server.soap.WhereExpr;

public class WhereCalc {
	private OlEval m_eval;

	public WhereCalc(){
		m_eval = new OlEval();
	}
	
	public WhereCalc(List<String> colNames, List<WhereExpr> whereExprs) {
		OlEvalCVRConvertor conv = new OlEvalCVRConvertor(colNames);
		for (WhereExpr expr : whereExprs) {
			conv.addWhereExpr(expr);
		}
		m_eval = conv.getOlEval();

	}

	boolean calc(final IRow row) {
		return m_eval.evaluate(new IOlColumnGetter() {

			public Object getValue(int idx) {
				return row.getObject(idx + 1);
			}
		});
	}

	public void addExpr(List<String>columns, String column, Object value) {
		m_eval.addExpr(columns.indexOf(column), new OlExprEquals(value));
	}

}
