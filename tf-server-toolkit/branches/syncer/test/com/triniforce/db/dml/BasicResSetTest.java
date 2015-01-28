/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.test.TFTestCase;

public class BasicResSetTest extends TFTestCase {
    
    static class MyResSet extends BasicResSet{
        @SuppressWarnings("serial")
        List<String> m_cols = new ArrayList<String>(){{add("col3"); add("col2"); add("col1");}};
        
        @Override
        public Object getObject(int columnIndex){
            return columnIndex * 10;
        }

        @Override
        public List<String> getColumns() {
            return m_cols;
        }

        @Override
        public boolean first() {
            return false;
        }

        @Override
        public boolean next() {
            return false;
        }
    };
    
    
    @Override
    public void test() throws Exception {
        
        IResSet rs = new MyResSet();
        try {
            rs.getIndexOf("col4");
            fail();
        } catch (EColumnNotFound e) {
            trace(e);
        }
        try {
            rs.getObject("col4");
            fail();
        } catch (EColumnNotFound e) {
            trace(e);
        }        
        assertEquals(1, rs.getIndexOf("col3"));
        assertEquals(2, rs.getIndexOf("col2"));
        assertEquals(3, rs.getIndexOf("col1"));
        
        assertEquals(10, rs.getObject("col3"));
        assertEquals(20, rs.getObject("col2"));
        assertEquals(30, rs.getObject("col1"));

    }

}
