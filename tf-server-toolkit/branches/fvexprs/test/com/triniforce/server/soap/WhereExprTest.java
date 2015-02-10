/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

import java.util.Set;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.soap.WhereExpr.ExprEquals;

public class WhereExprTest extends TFTestCase {
    
    @Override
    public void test(){
        {
            ExprEquals eq = new ExprEquals();
            eq.setColumnName("col1");
            eq.setValue("23");
            Set cols = eq.calcColumnNames();
            assertEquals(1, cols.size());
            
            VariantExpr.ColumnValue cv = new VariantExpr.ColumnValue("col2");
            eq.setValue(cv);
            cols = eq.calcColumnNames();
            assertEquals(2, cols.size());
            assertTrue(cols.contains("col1"));
            assertTrue(cols.contains("col2"));
            
        }
        
    }

}
