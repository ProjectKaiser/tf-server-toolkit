/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.outline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.eval.OlBExpr;
import com.triniforce.eval.OlBExprBetween;
import com.triniforce.eval.OlBExprColumnVsValue;
import com.triniforce.eval.OlBExprIN;
import com.triniforce.eval.OlEval;
import com.triniforce.eval.OlExprColumn;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.ExprV.ExprVColumn;
import com.triniforce.server.soap.WhereExpr;
import com.triniforce.server.soap.WhereExpr.ColumnExpr;
import com.triniforce.server.soap.WhereExpr.ExprBetween;
import com.triniforce.server.soap.WhereExpr.ExprColumnOr;
import com.triniforce.server.soap.WhereExpr.ExprIn;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.TFUtils;

/**
 * Converts CollectionViewRequest where objects into OlFilter objects
 * 
 * <pre>
 *  Variant 1
 *  
 *  OlEvalCVRConvertor conv = new OlEvalCVRConvertor(CollectionViewRequest cvr);
 *  OlEval f = conv.getOlEval();
 *  
 *  Variant 2
 *  
 *  OlEvalCVRConvertor conv = new OlEvalCVRConvertor(List<String> columns);
 *  conv.addWhereExpr(WhereExpr expr);
 *  conv.addEquals(String name, Object value);
 *  ...
 *  OlEval f = conv.getOlEval();
 * </pre>
 *  
 * 
 */

public class OlEvalCVRConvertor {
    
    public interface IAddOlExpr{
        public void addNamedOlExpr(String colName, OlBExpr olExpr);
    }
    
    final Map<String, Integer> m_nameToIdx;
    
    final OlEval m_eval = new OlEval();
    
    public OlEvalCVRConvertor(Map<String, Integer> columns){
        TFUtils.assertNotNull(columns, "columns are empty");
        m_nameToIdx = new HashMap<String, Integer>(columns);
    }
    
    public OlEvalCVRConvertor(List<String> columns){
        TFUtils.assertNotNull(columns, "columns are empty");
        m_nameToIdx = new HashMap<String, Integer>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            m_nameToIdx.put(columns.get(i), i);            
        }
    }
    
    public OlEvalCVRConvertor(CollectionViewRequest cvr){
        this(cvr.getColumns());
        if(null != cvr.getWhere()){
            for( String name: cvr.getWhere().keySet()){
                TFUtils.assertNotNull(name, "Null name in where clause");
                addEquals(name, cvr.getWhere().get(name));
            }
        }
        if(null != cvr.getWhereExprs()){
            for( WhereExpr expr: cvr.getWhereExprs()){
                addWhereExpr(expr);
            }
        }
    }
    
    public void addWhereExpr(WhereExpr expr){
        addToEval(m_eval, expr, 0);
    }
    
    public void addEquals(String colName, Object value){
        addWhereExpr(new WhereExpr.ExprEquals(colName, value));
    }
    
    public static OlBExpr convertWhereExpr(WhereExpr.ColumnExpr ce,  Map<String, Integer> colMap){
        String shortName = "OlBExpr" + ce.getClass().getSimpleName().substring(4);
        try {
            Class cl = Class.forName("com.triniforce.eval." + shortName);
            OlBExpr res = (OlBExpr) cl.newInstance();
            if(res instanceof OlBExprColumnVsValue){
                OlBExprColumnVsValue cv = (OlBExprColumnVsValue) res;
                Object testValue = ((WhereExpr.ColumnExprValued) ce).getValue();
                cv.setTestExpr(convertExprV(testValue, colMap));
            }
            return res;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }
    
    public static Object convertExprV(Object val,  Map<String, Integer> colMap){
        if(val instanceof ExprVColumn){
            ExprVColumn ev = (ExprVColumn) val;
            Integer idx = colMap.get(ev.getColumnName());
            TFUtils.assertNotNull(idx, "Index for column: " + ev.getColumnName());

            OlExprColumn ec = new OlExprColumn(idx);
            return ec;
        }
        return val;
    }
    
    Object cev(Object val){
        return convertExprV(val, m_nameToIdx);
    }
    
    
    private void addToEval(OlEval eval, WhereExpr aExpr, int level) {
        
        TFUtils.assertNotNull(aExpr, "expr is null");
        TFUtils.assertTrue(level <= 5, "Too deep recursion level");
        if(aExpr instanceof ExprColumnOr){
            ExprColumnOr colsOr = (ExprColumnOr) aExpr;
            if(null != colsOr.getColExprs()){
                OlEval or = new OlEval();
                or.setAndConcatenation(false);
                or.setNot(colsOr.isNot());
                for(ColumnExpr ce: colsOr.getColExprs()){
                    addToEval(or, ce, level++);
                }
                eval.addEval(or);
                return;
            }
        }
        
        if(! (aExpr instanceof ColumnExpr)){
            return;
        }
        ColumnExpr expr = (ColumnExpr) aExpr;
        
        OlBExpr dstExpr = null;
        if(expr instanceof WhereExpr.ExprIn){
            WhereExpr.ExprIn srcExpr = (ExprIn) expr;
            dstExpr = new OlBExprIN( srcExpr.getVals()); 
        }else if(expr instanceof WhereExpr.ExprBetween){
            WhereExpr.ExprBetween srcExpr = (ExprBetween) expr;
            dstExpr = new OlBExprBetween( cev(srcExpr.getFrom()), cev(srcExpr.getTo()));
        }else if(expr instanceof WhereExpr.ColumnExpr){
            dstExpr = convertWhereExpr(expr, m_nameToIdx);
        }
        
        TFUtils.assertNotNull(dstExpr, expr.toString());
        Integer idx = m_nameToIdx.get(expr.getColumnName());
        TFUtils.assertNotNull(idx, expr.getColumnName());
        
        if(aExpr.isNot()){
            OlEval evalNot = new OlEval();
            evalNot.setNot(true);
            evalNot.addExpr(idx, dstExpr);
            eval.addEval(evalNot);
        }else{        
            eval.addExpr(idx, dstExpr);
        }
        
    }

    public OlEval getOlEval(){
        return m_eval;
    }

}
