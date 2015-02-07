/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.util.Arrays;

import com.triniforce.db.test.TFTestCase;

public class MDSRowTest extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        IMDSRow r = new MDSRow();
        assertEquals(0, r.size());
        
        try {
            r.get(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            trace(e);
        }
        try {
            r.set(0, "s");
            fail();
        } catch (IndexOutOfBoundsException e) {
            trace(e);
        }
        
        assertEquals(0, r.size());
        int cnt = 0;
        for(@SuppressWarnings("unused") Object o: r){
            cnt++;
        }
        assertEquals(0, cnt);
        
    }
    
    public void testAdd(){
            IMDSRow r = new MDSRow();
            Object testData[] = new Object[]{"3", 2, 1L};
            for(Object o: testData){
                r.add(o);
            }
            assertEquals(testData.length, r.size());
            int idx = 0;
            for(Object v: r){
                assertEquals(testData[idx++], v);
            }
            idx = 0;
            for(Object v: r){
                assertEquals(r.get(idx++), v);
            }
        }
    
    public void testAppendArray(){
        {
            IMDSRow r = new MDSRow();
            Object testData[] = new Object[]{0,0,0, "3", 2, 1L};
            final int start = 3;
            r.appendArray(testData, start, testData.length - start);
            assertEquals(testData.length - start, r.size());
            assertEquals(testData[start], r.get(0));
            int idx = 0;
            for(Object v: r){
                assertEquals(testData[start + idx++], v);
            }
        }
        {
            IMDSRow r = new MDSRow();
            Object testData[] = new Object[]{"3", 2, 1L};
            r.appendArray(testData);
            assertEquals(testData[0], r.get(0));
        }
    }
    public void testAppendList(){
        {
            IMDSRow r = new MDSRow();
            Object testData[] = new Object[]{0,0,0, "3", 2, 1L};
            final int start = 3;
            r.appendList(Arrays.asList(testData), start, testData.length - start);
            assertEquals(testData.length - start, r.size());
            assertEquals(testData[start], r.get(0));
            int idx = 0;
            for(Object v: r){
                assertEquals(testData[start + idx++], v);
            }
        }
        {
            IMDSRow r = new MDSRow();
            Object testData[] = new Object[]{"3", 2, 1L};
            r.appendArray(testData);
            assertEquals(testData[0], r.get(0));
        }
    }    

}
