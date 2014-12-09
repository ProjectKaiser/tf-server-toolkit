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

    @SuppressWarnings("unused")
	void tstSplit(int srcCount, int splitSize){
        List src = new ArrayList();
        Random r = new Random();
        for(int i = 0;i<srcCount; i++){
            src.add(r.nextInt());
        }
        
        List dst = new ArrayList();
        ListSplitter ls = new ListSplitter(src, splitSize);
        
        int splitSizeCnt = 0;
        int smallerCnt = 0;
        for(List l: ls){
            assertTrue(l.size() <= splitSize);
            if(l.size() < splitSize) smallerCnt++;
            if(l.size() == splitSize) splitSizeCnt++;
            dst.addAll(l);
        }
        
        assertTrue(smallerCnt <=1);
        assertEquals(src, dst);
        
    }
    
    public void test(){

    	{
    		ListSplitter ls = new ListSplitter(null, 1);
    		assertFalse(ls.iterator().hasNext());
    	}
        
    	final int MAX_SRC_COUNT = 100;
    	for(int srcCount = 0; srcCount < MAX_SRC_COUNT; srcCount++){
    		for (int splitSize = 1; splitSize < MAX_SRC_COUNT + 10; splitSize++) {
    			tstSplit(srcCount, splitSize);
			}
    	}
    }

}
