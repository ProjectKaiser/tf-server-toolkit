/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.triniforce.db.test.TFTestCase;

public class ListSplitterTest extends TFTestCase {

    void testSplit(int srcCount, int splitSize){
        List src = new ArrayList();
        Random r = new Random();
        for(int i = 0;i<srcCount; i++){
            src.add(r.nextInt());
        }
        
        List dst = new ArrayList();
        ListSplitter ls = new ListSplitter(src, splitSize);
        
        for(List l: ls){
            assertTrue(l.size() <= splitSize);
            dst.addAll(l);
        }
        
        assertEquals(src, dst);
        
    }
    
    public void test(){
        
        ListSplitter ls = new ListSplitter(null, 1);
        assertFalse(ls.iterator().hasNext());
        
        List src = new ArrayList();
        ls = new ListSplitter(src, 1);
        assertFalse(ls.iterator().hasNext());        
        
        src.add(1);
        ls = new ListSplitter(src, 1);
        
    }

}
