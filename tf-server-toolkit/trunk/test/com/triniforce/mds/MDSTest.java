/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.util.Arrays;

import com.triniforce.db.test.TFTestCase;

public class MDSTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        MDS mds = new MDS();
        assertEquals(0, mds.getNamesMap().size());
        assertEquals(0, mds.getRows().size());
    }
    
    public void testAppend(){
        //add array
        {
            {
                MDS mds = new MDS();
                Object testData[] = new Object[]{0,0,0, "3", 2, 1L};
                final int start = 3;
                IMDSRow r = mds.addArray(testData, start, testData.length - start);
                assertEquals(testData.length - start, r.size());
                assertEquals(testData[start], r.get(0));
                int idx = 0;
                for(Object v: r){
                    assertEquals(testData[start + idx++], v);
                }
                assertEquals(1, mds.getRows().size());
            }
            {
                MDS mds = new MDS();
                Object testData[] = new Object[]{"3", 2, 1L};
                IMDSRow r = mds.addArray(testData);
                assertEquals(testData[0], r.get(0));
                assertEquals(1, mds.getRows().size());
            }
        }
        //add list
        {
            {
                MDS mds = new MDS();
                Object testData[] = new Object[]{0,0,0, "3", 2, 1L};
                final int start = 3;
                IMDSRow r = mds.addList(Arrays.asList(testData), start, testData.length - start);
                assertEquals(testData.length - start, r.size());
                assertEquals(testData[start], r.get(0));
                int idx = 0;
                for(Object v: r){
                    assertEquals(testData[start + idx++], v);
                }
                assertEquals(1, mds.getRows().size());
            }
            {
                MDS mds = new MDS();
                Object testData[] = new Object[]{"3", 2, 1L};
                IMDSRow r = mds.addList(Arrays.asList(testData));
                assertEquals(testData[0], r.get(0));
                assertEquals(1, mds.getRows().size());
            }
        }
    }
    
    public void testIterator(){
        MDS mds = new MDS();
        mds.addArray(new Object[]{1, 2, 3});
        mds.addArray(new Object[]{"4", 5, 6});
        mds.addArray(new Object[]{7, "8", 9});
        assertEquals(3, mds.getRows().size());

        Object testData[] = new Object[]{1, 2, 3, "4", 5, 6, 7, "8", 9};
        int idx = 0;
        for(IMDSRow r: mds){
            for(Object v:r){
                assertEquals(testData[idx++], v);
            }
        }
    }
}
