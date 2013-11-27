/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.triniforce.postoffice.intf.IPOBox;
import com.triniforce.postoffice.intf.IPostMaster;
import com.triniforce.postoffice.intf.LTRAddStreetOrBoxes;
import com.triniforce.postoffice.intf.LTRListBoxes;
import com.triniforce.postoffice.intf.LTRListStreets;
import com.triniforce.postoffice.intf.NamedPOBoxes;
import com.triniforce.postoffice.intf.StreetPath;

public class PostMasterBoxTest extends TestCase {
    
    static class Box1 extends TestPOBox{};
    static class Box2 extends TestPOBox{};
    static class Box3 extends TestPOBox{};
    
    public void testBoxes(){
        IPostMaster pm = new PostMaster();
        
        //empty, IPostMaster.class
        {
            Map<String, UUID> res = pm.call(null, null, new LTRListBoxes());
            assertEquals(1, res.size());
            assertNotNull(res.get(IPostMaster.class.getName()));
        }
 
        
        //add a street with two boxes then one more box
        {
        
            NamedPOBoxes nboxes1_2 = new NamedPOBoxes();
            nboxes1_2.putByClass(new Box1());
            nboxes1_2.putByClass(new Box2());
        
            pm.call(null, null, new LTRAddStreetOrBoxes(null, "street1", nboxes1_2));
        
            Map<String, UUID> res1_2 = pm.call(null, null, new LTRListBoxes(new StreetPath("street1")));
            assertEquals(2, res1_2.size());
            
            NamedPOBoxes nboxes3 = new NamedPOBoxes();
            nboxes3.putByClass(new Box3());
            
            pm.call(null, null, new LTRAddStreetOrBoxes(new StreetPath("street1"), null, nboxes3));
            
            Map<String, UUID> res3 = pm.call(null, null, new LTRListBoxes(new StreetPath("street1")));
            
            assertEquals(3, res3.size());
            assertNotSame(res1_2, res3);
            
            for( IPOBox box: nboxes1_2.values()){
                TestPOBox tbox = (TestPOBox) box;
                assertTrue(tbox.priorProcessCalled);
            }
            
        }
    }
    
    public void testStreets(){
        
        IPostMaster pm = new PostMaster();
        
        //empty root
        {
            List<String> res = pm.call(null, null, new LTRListStreets());
            assertEquals(0, res.size());
        }
        
        //add empty street
        {
            
            //add street1
            
            assertNull(pm.call(null, null, new LTRAddStreetOrBoxes(null, "street1", null)));
            
            List<String> res = pm.call(null, null, new LTRListStreets());
            assertEquals(1, res.size());
            assertTrue(res.contains("street1"));
            
            //add street2
            
            assertNull(pm.call(null, null, new LTRAddStreetOrBoxes(null, "street2", null)));
            
            List<String> res2 = (List<String>) pm.call(null, null, new LTRListStreets());
            assertEquals(2, res2.size());
            assertTrue(res2.contains("street1"));
            assertTrue(res2.contains("street2"));
            
            //first result should not be changes since its a copy
            assertEquals(1, res.size());
            assertTrue(res.contains("street1"));            
            
        }
    }

}
