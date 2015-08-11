/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import com.triniforce.db.test.TFTestCase;

/**
 *
 * Ref. also HudsonApiInvTest
 *
 **/
public class HudsonApiTest extends TFTestCase {
    
    //http://gmpxp:8080/hudson/job/toolkit/api/json
    //http://gmpxp:8080/hudson/api/json?tree=jobs[name]
    
    @Override
    public void test() throws Exception {
        {
            HudsonApi h = new HudsonApi("http://gmpxp:8080/hudson", "" , "");
            assertEquals("http://gmpxp:8080/hudson/", h.getBaseAddress());
            h = new HudsonApi("http://gmpxp:8080/hudson/", "","");
            assertEquals("http://gmpxp:8080/hudson/", h.getBaseAddress());
        }
    }
    
    public void testFullUrl(){
        HudsonApi h = new HudsonApi("http://gmpxp:8080/hudson", "", "");
        assertEquals( "http://gmpxp:8080/hudson/api/json", h.calcFullUrl(null, null, "json", null));
        Map<String, String> q = new LinkedHashMap<String, String>();
        q.put("tree", "jobs[name]");
        q.put("arg2", "val2");
        assertEquals( "http://gmpxp:8080/hudson/api/json?tree=jobs%5Bname%5D&arg2=val2", h.calcFullUrl(null, null, "json", q));
        assertEquals( "http://gmpxp:8080/hudson/job/toolkit/api/json", h.calcFullUrl("job", "toolkit", "json", null));
        assertEquals( "http://gmpxp:8080/hudson/job/toolkit/api/json", h.calcFullUrl("job", "toolkit", "json", null));
        assertEquals("http://gmpxp:8080/hudson/job/task%201%201/api/json",h.calcFullUrl("job", "task 1 1", "json", null)); 
        
    }
    
}
