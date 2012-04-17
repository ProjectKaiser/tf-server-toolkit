/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.util.ArrayList;
import java.util.Arrays;

import com.triniforce.db.test.TFTestCase;
public class OrderTest extends TFTestCase {

    static class TestC1{
        private Integer m_v;
        public TestC1(Integer v) {
            m_v = v;
        }    
        @Override
        public boolean equals(Object obj) {
            return m_v.equals(((TestC1)obj).m_v);
        }
        @Override
        public String toString() {
            return m_v.toString();
        }
    }
    
    public void testOrderBy(){
        ArrayList<TestC1> list = new ArrayList<TestC1>(
                Arrays.asList(new TestC1(1), new TestC1(2), new TestC1(3), new TestC1(4)));
        Order.orderBy(list,
                Arrays.asList("v5", "v2", "v1", "v6", "v4"), new Order.IKeyGetter<TestC1, String>(){
                    public String getKey(TestC1 v) {
                        return "v"+v.m_v;
                    }
                });
        assertEquals(Arrays.asList(new TestC1(2), new TestC1(1), new TestC1(4), new TestC1(3)), list);
        
    }
}
