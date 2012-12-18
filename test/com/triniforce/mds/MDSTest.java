/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.triniforce.db.dml.IResSet;
import com.triniforce.db.test.TFTestCase;

public class MDSTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        MDS mds = new MDS();
        assertEquals(0, mds.getNamesMap().size());
        assertEquals(0, mds.getRows().size());
    }
    
    public void testAppendNames(){
        MDS mds = new MDS();
        Map<String, Integer> names = new HashMap<String, Integer>();
        names.put("col1", 2);
        names.put("col2", 6);
        names.put("col4", 2);
        mds.setNamesMap(names);
        mds.appendNames(Arrays.asList(new String[]{"col3", "col5"}));
        assertEquals( (Integer)7, mds.getNamesMap().get("col3"));
        assertEquals( (Integer)8, mds.getNamesMap().get("col5"));
    }
    
    public void testAppend(){
        //add array
        {
            {
                MDS mds = new MDS();
                Object testData[] = new Object[]{0,0,0, "3", 2, 1L};
                final int start = 3;
                IMDSRow r = mds.appendRowAsArray(testData, start, testData.length - start);
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
                IMDSRow r = mds.appendRowAsArray(testData);
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
                IMDSRow r = mds.appendRowAsList(Arrays.asList(testData), start, testData.length - start);
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
                IMDSRow r = mds.appendRowAsList(Arrays.asList(testData));
                assertEquals(testData[0], r.get(0));
                assertEquals(1, mds.getRows().size());
            }
        }
    }
    
    public void testIterator(){
        MDS mds = new MDS();
        mds.appendRowAsArray(new Object[]{1, 2, 3});
        mds.appendRowAsArray(new Object[]{"4", 5, 6});
        mds.appendRowAsArray(new Object[]{7, "8", 9});
        assertEquals(3, mds.getRows().size());

        Object testData[] = new Object[]{1, 2, 3, "4", 5, 6, 7, "8", 9};
        int idx = 0;
        for(IMDSRow r: mds){
            for(Object v:r){
                assertEquals(testData[idx++], v);
            }
        }
    }
    
    public void testGetIResSet(){
            MDS mds = new MDS();
            mds.appendRowAsArray(new Object[]{1, 2, 3});
            mds.appendRowAsArray(new Object[]{"4", 5, 6});
            mds.appendRowAsArray(new Object[]{7, "8", 9});
            assertEquals(3, mds.getRows().size());
            
            IResSet rs = mds.getIResSet();
    
            Object testData[] = new Object[]{1, 2, 3, "4", 5, 6, 7, "8", 9};
            int idx = 0;
            while(rs.next()){
                for(int i=0; i< 3; i++){
                    assertEquals(testData[idx++], rs.getObject(i));    
                }
            }
            assertEquals(9, idx);
        }
    
}
