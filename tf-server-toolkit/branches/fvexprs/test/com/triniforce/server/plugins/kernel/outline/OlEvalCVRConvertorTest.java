/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.outline;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.eval.OlBExprBetween;
import com.triniforce.eval.OlBExprContains;
import com.triniforce.eval.OlBExprGE;
import com.triniforce.eval.OlBExprIN;
import com.triniforce.eval.OlBExprLE;
import com.triniforce.eval.OlBExprNotNull;
import com.triniforce.eval.OlEval;
import com.triniforce.eval.Ol_IdxExpr;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.WhereExpr;
import com.triniforce.utils.EUtils.EAssertNotNullFailed;

public class OlEvalCVRConvertorTest extends TFTestCase {
    
    CollectionViewRequest generateCvr(){
        CollectionViewRequest cvr = new CollectionViewRequest();
        cvr.getColumns().add("expr_cIn");
        cvr.getColumns().add("expr_cBetween");
        cvr.getColumns().add("where2");
        cvr.getColumns().add("where1");
        cvr.getColumns().add("not filtered");
        return cvr;
        
    }
        
    public void testCVRConstructor() throws Exception {
        
        //null columns
        {
            CollectionViewRequest cvr = new CollectionViewRequest();
            cvr.setColumns(null);
            try {
                new OlEvalCVRConvertor(cvr);
                fail();
            } catch (EAssertNotNullFailed e) {
                trace(e);
            }
        }
        
        //null IN
        {
            CollectionViewRequest cvr = generateCvr();
            cvr.getWhereExprs().add(new WhereExpr.ExprIn("expr_cIn", null));
  //          OlFilter f = conv.getOlFilter();
//            f.testValues(new Object[]{}, 0);
            try {
                new OlEvalCVRConvertor(cvr);
                fail();
            } catch (EAssertNotNullFailed e) {
                trace(e);
            }
        }
        //null between
        {
            CollectionViewRequest cvr = generateCvr();
            cvr.getWhereExprs().add(new WhereExpr.ExprBetween("expr_cBetween", null, 1));
            try {
                new OlEvalCVRConvertor(cvr);
                fail();
            } catch (EAssertNotNullFailed e) {
                trace(e);
            }
            cvr = generateCvr();
            cvr.getWhereExprs().add(new WhereExpr.ExprBetween("expr_cBetween", 1, null));
            try {
                new OlEvalCVRConvertor(cvr);
                fail();
            } catch (EAssertNotNullFailed e) {
                trace(e);
            }
        }
        
        //whereExpr conversion
        {
            CollectionViewRequest cvr = generateCvr();
            cvr.getWhereExprs().add(new WhereExpr.ExprBetween("expr_cBetween", 1, 3));
            OlEvalCVRConvertor conv = new OlEvalCVRConvertor(cvr);
            OlEval f = conv.getOlEval();
            assertTrue(f.evalArray(new Object[]{0,1,2,3,4,}, 0));
            assertFalse(f.evalArray(new Object[]{0,0,2,3,4,}, 0));
        }
        //where and whereExpr conversion
        {
            CollectionViewRequest cvr = generateCvr();
            cvr.getWhereExprs().add(new WhereExpr.ExprBetween("expr_cBetween", 1, 3));
            cvr.getWhere().put("where2", "22");

            OlEvalCVRConvertor conv = new OlEvalCVRConvertor(cvr);
            OlEval f = conv.getOlEval();
            assertTrue(f.evalArray(new Object[]{0,1,"22",3,4,}, 0));
            assertFalse(f.evalArray(new Object[]{0,0,"22",3,4,}, 0));
            assertFalse(f.evalArray(new Object[]{0,1,"23",3,4,}, 0));
        }
    }
    
    @Override
    public void test() throws Exception {
        List<String> cols = new ArrayList<String>();
        cols.add("cIn");        
        cols.add("cBetween");
        cols.add("cNotNull");
        cols.add("cContains");

        
        OlEvalCVRConvertor conv = new OlEvalCVRConvertor(cols);
        //Between
        {
            WhereExpr.ExprBetween expr = new WhereExpr.ExprBetween();
            expr.setColumnName("cBetween");
            expr.setFrom(12);
            expr.setTo(15);
            conv.addWhereExpr(expr);
        }
        //In
        {
            WhereExpr.ExprIn expr = new WhereExpr.ExprIn();
            expr.setColumnName("cIn");
            expr.setVals(new Object[]{100, 5, 100500});
            conv.addWhereExpr(expr);
        }
        //NotNull
        {
            WhereExpr.ExprNotNull expr = new WhereExpr.ExprNotNull();
            expr.setColumnName("cNotNull");
            conv.addWhereExpr(expr);
        }
        //Contains
        {
            conv.addWhereExpr(new WhereExpr.ExprContains("cContains", "tHe"));
        }
        
        OlEval f = conv.getOlEval();
        
        //check between
        {
            int idx = 0;
            int colIdx = 1;
            OlBExprBetween olExpr = (OlBExprBetween) ((Ol_IdxExpr)f.getEvaluators().get(idx)).getExpr();
            assertEquals(colIdx, ((Ol_IdxExpr)f.getEvaluators().get(idx)).getIdx());
            OlBExprGE ge = olExpr.getLeftExpr();
            OlBExprLE le = olExpr.getRightExpr();
            assertEquals(12, ge.getTestValue());
            assertEquals(15, le.getTestValue());
        }
        //check in
        {
            int idx = 1;
            int colIdx = 0;
            OlBExprIN olExpr = (OlBExprIN) ((Ol_IdxExpr)f.getEvaluators().get(idx)).getExpr();
            assertEquals(colIdx, ((Ol_IdxExpr)f.getEvaluators().get(idx)).getIdx());
            
            //test in
            OlEval ff = new OlEval();
            ff.addExpr(0, olExpr);
            assertTrue(ff.evalArray(new Object[]{5}, 0));
            assertFalse(ff.evalArray(new Object[]{50}, 0));
            assertTrue(ff.evalArray(new Object[]{100}, 0));
            assertFalse(ff.evalArray(new Object[]{1000}, 0));
            assertTrue(ff.evalArray(new Object[]{100500}, 0));
            assertFalse(ff.evalArray(new Object[]{1005000}, 0));
        }
        
        //check NotNull
        {
            int idx = 2;
            int colIdx = 2;
            @SuppressWarnings("unused")
            OlBExprNotNull olExpr = (OlBExprNotNull) ((Ol_IdxExpr)f.getEvaluators().get(idx)).getExpr();
            assertEquals(colIdx, ((Ol_IdxExpr)f.getEvaluators().get(idx)).getIdx());            
        }
        
        //check Contains
        {
            int idx = 3;
            int colIdx = 3;
            OlBExprContains olExpr = (OlBExprContains) ((Ol_IdxExpr)f.getEvaluators().get(idx)).getExpr();
            assertEquals(colIdx, ((Ol_IdxExpr)f.getEvaluators().get(idx)).getIdx());
            OlEval ff = new OlEval();
            ff.addExpr(0, olExpr);
            assertFalse(ff.evalArray(new Object[]{5}, 0));
            assertTrue(ff.evalArray(new Object[]{"The"}, 0));
            assertTrue(ff.evalArray(new Object[]{"   The"}, 0));
            assertFalse(ff.evalArray(new Object[]{"   T he"}, 0));
        }
    }
    
    public void testContainsWord(){
        List<String> cols = new ArrayList<String>();
        cols.add("c1");
        OlEvalCVRConvertor c = new OlEvalCVRConvertor(cols);
        c.addWhereExpr(new WhereExpr.ExprContainsWord("c1", "maXim.ge"));
        OlEval e = c.getOlEval();
        assertTrue(e.evalArray(new Object[]{"a maxIm.ge@gmail.com"}, 0));
        assertFalse(e.evalArray(new Object[]{"amaxIm.ge@gmail.com"}, 0));
    }
    
    public void testOr(){
        List<String> cols = new ArrayList<String>();
        cols.add("c1");
        cols.add("c2");
        OlEvalCVRConvertor c = new OlEvalCVRConvertor(cols);
        WhereExpr.ExprColumnOr or = new WhereExpr.ExprColumnOr();
        or.getColExprs().add(new WhereExpr.ExprEquals("c1", "1"));
        or.getColExprs().add(new WhereExpr.ExprEquals("c2", "2"));
        
        c.addWhereExpr(or);
        OlEval e = c.getOlEval();
        assertTrue(e.evalArray(new Object[]{"1", "2"}, 0));
        assertTrue(e.evalArray(new Object[]{"1", "1"}, 0));
        assertTrue(e.evalArray(new Object[]{"2", "2"}, 0));
        assertFalse(e.evalArray(new Object[]{"3", "3"}, 0));
    }
    
    public void testNot(){
        List<String> cols = new ArrayList<String>();
        cols.add("c1");
        cols.add("c2");
        OlEvalCVRConvertor c = new OlEvalCVRConvertor(cols);
        WhereExpr.ExprIn notIn = new WhereExpr.ExprIn("c2", new Object[]{"str1", "str2"});
        notIn.setNot(true);
        c.addWhereExpr(notIn);
        OlEval e = c.getOlEval();
        trace(e);
        assertTrue(e.evalArray(new Object[]{"str1", "str3"}, 0));
        assertFalse(e.evalArray(new Object[]{"str1", "str1"}, 0));
    }

}