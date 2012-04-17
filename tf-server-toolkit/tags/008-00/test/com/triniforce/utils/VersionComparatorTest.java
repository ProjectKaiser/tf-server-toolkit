/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.db.test.TFTestCase;

public class VersionComparatorTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        String s1 = null;
        String s2 = null;
        assertEquals(0, VersionComparator.compareVersions(s1, s2));
        assertEquals(0, VersionComparator.compareVersions("", ""));
        assertEquals(0, VersionComparator.compareVersions("1.0", "1.0"));
        assertTrue(VersionComparator.compareVersions("1a.0", "1.0") > 0);
        assertTrue(VersionComparator.compareVersions("1a.0", "1.87") > 0);
        assertTrue(VersionComparator.compareVersions("1.87", "1a.0") < 0);
        assertTrue(VersionComparator.compareVersions("2.0", "1.87") > 0);
        assertTrue(VersionComparator.compareVersions("2.0", "1.abcd") > 0);
        assertTrue(VersionComparator.compareVersions("2.0abc", "2.0abcd") < 0);
        assertTrue(VersionComparator.compareVersions("2.0abcde", "2.0abcd") > 0);
        assertTrue(VersionComparator.compareVersions("2.0abcde", "2.0abcde") == 0);
        assertTrue(VersionComparator.compareVersions("2.0abcde.1", "2.0abcde") >0);
        assertTrue(VersionComparator.compareVersions("2.0abcde.1", "2.0abcde.a") <0);
    }
    
    public void testCollection(){
        //empty
        {
            Map<String, String> V1 = new HashMap<String, String>();
            Map<String, String> V2 = new HashMap<String, String>();
            assertTrue( VersionComparator.compareVersions(V1, V2) == 0);
        }
        //{p1=1.0, p2=1.0} equals {p1=1.0, p2=1.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "1.0");
            V1.put("p2", "1.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p1", "1.0");            
            V2.put("p2", "1.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) == 0);
        }
        //{p2=1.0, p1=1.0} equals {p1=1.0, p2=1.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "1.0");
            V1.put("p2", "1.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p2", "1.0");            
            V2.put("p1", "1.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) == 0);
        }  
        //{p1=1.0, p2=2.0} greater {p1=1.0, p2=1.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "1.0");
            V1.put("p2", "2.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p1", "1.0");            
            V2.put("p2", "1.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) > 0);
        }   
        //{p1=1.0, p2=1.0, p3=1.0} greater {p1=1.0, p2=1.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "1.0");
            V1.put("p2", "1.0");
            V1.put("p3", "1.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p1", "1.0");            
            V2.put("p2", "1.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) > 0);
        }  
        //{p1=1.0, p2=2.0} less {p1=1.0, p2=3.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "1.0");
            V1.put("p2", "2.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p1", "1.0");            
            V2.put("p2", "3.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) < 0);
        }
        //{p1=1.0, p2=3.0} equals {p1=1.0, p2=3.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "1.0");
            V1.put("p2", "3.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p1", "1.0");            
            V2.put("p2", "3.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) == 0);
        }
        //{p1=1.0, p2=3.0} less {p1=1.0, p2=3.0, p3 = 1.a.07}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "1.0");
            V1.put("p2", "3.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p1", "1.0");            
            V2.put("p2", "3.0");
            V2.put("p3", "1.a.07");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) < 0);
        } 
        //{p1=5.0} less {p2=1.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p1", "5.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p2", "1.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) < 0);
        }
        //{p1=5.0, p2=1.0} equals {p2=1.0}
        {
            Map<String, String> V1 = new HashMap<String, String>();
            V1.put("p2", "1.0");
            V1.put("p1", "5.0");
            Map<String, String> V2 = new HashMap<String, String>();
            V2.put("p2", "1.0");
            trace(V1);
            assertTrue( VersionComparator.compareVersions(V1, V2) > 0);
        }
        
    }
    

}
