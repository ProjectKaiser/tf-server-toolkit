/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import com.triniforce.utils.IName;

public class OrderByClause extends Expr.List {
	
	public static class DescColumn extends Expr.Column{

		public DescColumn(String tablePrefix, String col) {
			super(tablePrefix, col);
		}
		
		public DescColumn(String tablePrefix, IName col) {
			super(tablePrefix, col);
		}
		
		@Override
		public String toString() {
			String res = super.toString();
			res += " DESC";
			return res;
		}
		
	}
	
    @Override
    public OrderByClause add(Expr expr) {
        if( ! ( expr instanceof Column ) ){
            throw new Err.ENotAllowedExprType(expr.getClass().getName());
        }
        super.add(expr);
        return this;
        
    }
    public OrderByClause addCol(String prefix, String col) {
        add( new Column(prefix, col));
        return this;
    }
    
    @Override
    public String toString() {
        String s = super.toString();
        if( s.length() == 0) return ""; //$NON-NLS-1$
        return "order by " + s; //$NON-NLS-1$
    }
}
