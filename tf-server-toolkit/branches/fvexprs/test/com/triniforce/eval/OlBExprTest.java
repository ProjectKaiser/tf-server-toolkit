/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eval;

import com.triniforce.db.test.TFTestCase;

public class OlBExprTest extends TFTestCase {

    
    void printExpr(String exprName, OlBExpr bexpr){
        
        OlEval ev = new OlEval();
        ev.addExpr(2, bexpr);
        trace( exprName +" " + ev);
    }
    
    public void test(){
        
        printExpr("=", new OlBExprEquals(5));
        printExpr("=", new OlBExprEquals(null));
        printExpr(">", new OlBExprGE(new OlExprColumn(22)));
        printExpr(">", new OlBExprGE(12));
        printExpr("<", new OlBExprLE(new OlExprColumn(22)));
        printExpr("<", new OlBExprLE(12));
        printExpr("between", new OlBExprBetween(new OlExprColumn(12), 4));
        printExpr("in", new OlBExprIN(new Object[]{1,new OlExprColumn(22),3}));
        
        printExpr("contains", new OlBExprContains("THE"));
        printExpr("contains word", new OlBExprContainsWord("THE"));
        
    }


}
