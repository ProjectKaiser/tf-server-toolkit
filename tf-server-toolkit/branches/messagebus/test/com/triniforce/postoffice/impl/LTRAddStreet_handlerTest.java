/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.List;

import com.triniforce.postoffice.intf.IPostMaster;
import com.triniforce.postoffice.intf.LTRAddStreet;
import com.triniforce.postoffice.intf.LTRGetStreets;

import junit.framework.TestCase;

public class LTRAddStreet_handlerTest extends TestCase {
    public void test(){
        
        IPostMaster pm = new PostMaster();
        
        //empty root
        {
            List<String> res = pm.call(null, null, new LTRGetStreets());
            assertEquals(0, res.size());
        }
        
        //add empty street
        {
            
            //add street1
            
            assertNull(pm.call(null, null, new LTRAddStreet(null, "street1", null)));
            
            List<String> res = pm.call(null, null, new LTRGetStreets());
            assertEquals(1, res.size());
            assertTrue(res.contains("street1"));
            
            //add street2
            
            assertNull(pm.call(null, null, new LTRAddStreet(null, "street2", null)));
            
            List<String> res2 = (List<String>) pm.call(null, null, new LTRGetStreets());
            assertEquals(2, res2.size());
            assertTrue(res2.contains("street1"));
            assertTrue(res2.contains("street2"));
            
            //first result should not be changes since its a copy
            assertEquals(1, res.size());
            assertTrue(res.contains("street1"));            
            
        }
    }

}
