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
import com.triniforce.mds.MDS.ColumnNotFound;
import com.triniforce.utils.IName;

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
    
    public void testGetCell() {
    	
    	MDS mds = new MDS();
    	IMDSRow row = new MDSRow();
    	//
    	class Name implements IName {
			private String m_name;
    		public Name(String name) {
				m_name = name;
			}
    		public String getName() {
				return m_name;
			}
		}
    	
    	IName name = null; 
    	
    	try {
    		mds.getCell(row, name);
    		fail();
    	} catch (NullPointerException e) {
			assertEquals(e.getMessage(),"col");
		}
    	
    	//
    	name = new Name("name");
    	try {
    		mds.getCell(null, name);
    		fail();
    	} catch (NullPointerException e) {
			assertEquals(e.getMessage(),"row");
		}
    	
    	//
    	name = new Name(null);
    	try {
    		mds.getCell(row, name);
    		fail();
    	} catch (NullPointerException e) {
			assertEquals(e.getMessage(),"col");
		}
    	
    	//
    	name = new Name("col1");
    	try {
    		mds.getCell(row, name);
    		fail();
    	} catch (ColumnNotFound e) {
			assertEquals(e.getMessage(),"Column name: col1");
		}
    	
    	//
    	mds.appendNames(Arrays.asList(new String[]{"col1", "col2", "col3", "col4"}));
    	mds.appendRowAsList(Arrays.asList(new Object[]{"3FFF", null, 13L, "AAAA"}));
    	mds.appendRowAsList(Arrays.asList(new Object[]{"3", null, 10L, "BBBBBB"}));
    	mds.appendRowAsList(Arrays.asList(new Object[]{"233", 456, null, "CCCCCCC"}));
    	
    	assertEquals(mds.getCell(mds.getRows().get(0),new Name("col1")),"3FFF");
    	assertNull(mds.getCell(mds.getRows().get(0),new Name("col2")));
    	assertEquals(mds.getCell(mds.getRows().get(1),new Name("col3")),10L);
    	assertEquals(mds.getCell(mds.getRows().get(2),new Name("col4")),"CCCCCCC");
        	
    }
    
    public void testAppendRow() {
    	
    	MDS mds = new MDS();
    	//
    	try {
    		mds.appendRow(null);
    	} catch (NullPointerException e) {
			assertEquals(e.getMessage(),"src");
		}
    	//
    	IMDSRow row1 = new MDSRow();
    	IMDSRow res = mds.appendRow(row1);
    	assertEquals(mds.getRows().size(),1);
    	assertSame(mds.getRows().get(0),row1);
    	assertSame(mds.getRows().get(0),res);
    	// 	
    	IMDSRow row2 = new MDSRow();
    	res = mds.appendRow(row2);
    	row2.add(1L); row2.add("AA"); row2.add(null);  
    	
    	assertEquals(mds.getRows().size(),2);
    	assertSame(mds.getRows().get(0),row1);
    	assertSame(mds.getRows().get(1),row2);
    	assertSame(mds.getRows().get(1),res);
    	
    	assertEquals(mds.getRows().get(1).get(0),1L);
    	assertEquals(mds.getRows().get(1).get(1),"AA");
    	assertNull(mds.getRows().get(1).get(2));
       	
    }
    
    public void testToString() {
    	
    	//
    	MDS mds = new MDS();
    	assertEquals(mds.toString(),"\n\n");
    	
    	//
    	mds = new MDS();
    	mds.appendNames(Arrays.asList(new String[]{"col1", "col2", "col3", "col4"}));
    	mds.appendRowAsList(Arrays.asList(new Object[]{"3FFF", null, 13L, "AAAA"}));
    	mds.appendRowAsList(Arrays.asList(new Object[]{"3", null, 10L, "BBBBBB"}));
    	mds.appendRowAsList(Arrays.asList(new Object[]{"233", 456, null, "CCCCCCC"}));
        
    	String s = mds.toString();
    	assertNotNull(s);
    	trace(s);
    
    }
    
}
