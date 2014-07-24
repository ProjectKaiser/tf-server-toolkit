/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.syncbus.intf.IEntireOffer;
import com.triniforce.syncbus.intf.IEntirePub;

public class SyncBusTest extends TFTestCase {
    
    @SuppressWarnings("unused")
    public void test() {
        // Test constructor
        SyncBus sb = new SyncBus();
    }
    
    public void test_registerEntirePub(){
        //test
        {
            SyncBus sb = new SyncBus();
            Mockery ctx = new Mockery();
            final IEntirePub eb = ctx.mock(IEntirePub.class);
            
            sb.registerEntirePub(eb);
            
            ctx.checking(new Expectations(){{
                exactly(1).of(eb).setIEntireOffer(with(any(IEntireOffer.class)));
            }});
            ctx.assertIsSatisfied();
            
        }        
    }
    

}
