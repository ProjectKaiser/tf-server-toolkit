/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import com.triniforce.db.qbuilder.Expr.Brackets;

public class WhereClause extends Brackets{
    @Override
    public String toString() {
        String res = super.toString();
        if(res.length()==0) return res;
        return "where " + res; //$NON-NLS-1$
    }
    @Override
    public WhereClause and(Expr op) {
        super.and(op);
        return this;
    }    
    @Override
    public WhereClause or(Expr op) {
        super.or(op);
        return this;
    }
    
    public WhereClause andCompare(String tablePrefix, String col, String sop){
        return this.and( new Compare(tablePrefix, col, sop, null, null));
    }
    public WhereClause andCompare(String tablePrefix, String col, String sop, String tablePrefix2, String col2){
        return this.and( new Compare(tablePrefix, col, sop, tablePrefix2, col2));
    }    
    
    public WhereClause orCompare(String tablePrefix, String col, String sop){
        return this.or( new Compare(tablePrefix, col, sop, null, null));
    }    
    public WhereClause orCompare(String tablePrefix, String col, String sop, String tablePrefix2, String col2){
        return this.or( new Compare(tablePrefix, col, sop, tablePrefix2, col2));
    }    
    
}
