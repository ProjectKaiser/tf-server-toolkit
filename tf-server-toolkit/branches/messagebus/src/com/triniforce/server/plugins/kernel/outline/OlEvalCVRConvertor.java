/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.outline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.eval.OlEval;
import com.triniforce.eval.OlExpr;
import com.triniforce.eval.OlExprBetween;
import com.triniforce.eval.OlExprContains;
import com.triniforce.eval.OlExprContainsWord;
import com.triniforce.eval.OlExprEquals;
import com.triniforce.eval.OlExprIN;
import com.triniforce.eval.OlExprNotNull;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.WhereExpr;
import com.triniforce.server.soap.WhereExpr.ColumnExpr;
import com.triniforce.server.soap.WhereExpr.ExprBetween;
import com.triniforce.server.soap.WhereExpr.ExprColumnOr;
import com.triniforce.server.soap.WhereExpr.ExprIn;
import com.triniforce.utils.TFUtils;

/**
 * Converts CollectionViewRequest where objects into OlFilter objects
 * 
 * <pre>
 *  Variant 1
 *  
 *  OlEvalCVRConvertor conv = new OlEvalCVRConvertor(CollectionViewRequest cvr);
 *  OlEval f = conv.getOlFilter();
 *  
 *  Variant 2
 *  
 *  OlEvalCVRConvertor conv = new OlEvalCVRConvertor(List<String> columns);
 *  conv.addWhereExpr(WhereExpr expr);
 *  conv.addEquals(String name, Object value);
 *  ...
 *  OlFilter f = conv.getOlEval();
 * </pre>
 *  
 * 
 */

public class OlEvalCVRConvertor {
    
    public interface IAddOlExpr{
        public void addNamedOlExpr(String colName, OlExpr olExpr);
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
        
        OlExpr dstExpr = null;
        if(expr instanceof WhereExpr.ExprIn){
            WhereExpr.ExprIn srcExpr = (ExprIn) expr;
            dstExpr = new OlExprIN( ((WhereExpr.ExprIn)srcExpr).getVals()); 
        }else if(expr instanceof WhereExpr.ExprBetween){
            WhereExpr.ExprBetween srcExpr = (ExprBetween) expr;
            dstExpr = new OlExprBetween(srcExpr.getFrom(), srcExpr.getTo());
        }else if(expr instanceof WhereExpr.ExprNotNull){
            dstExpr = new OlExprNotNull();
        }else if(expr instanceof WhereExpr.ExprContains){
            dstExpr = new OlExprContains(((WhereExpr.ExprContains) expr).getValue());
        }else if(expr instanceof WhereExpr.ExprContainsWord){
            dstExpr = new OlExprContainsWord(((WhereExpr.ExprContainsWord) expr).getValue());
        }else if(expr instanceof WhereExpr.ExprEquals){
            dstExpr = new OlExprEquals(((WhereExpr.ExprEquals) expr).getValue());
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
