/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.qbuilder;


public class GroupByClause extends Expr.List {

    @Override
    public GroupByClause add(Expr expr) {
        if( ! ( expr instanceof Column ) ){
            throw new Err.ENotAllowedExprType(expr.getClass().getName());
        }
        super.add(expr);
        return this;
        
    }
    public GroupByClause addCol(String prefix, String col) {
        add( new Column(prefix, col));
        return this;
    }
	
	@Override
    public String toString() {
        String s = super.toString();
        if( s.length() == 0) return ""; //$NON-NLS-1$
        return "group by " + s; //$NON-NLS-1$
    }
}
