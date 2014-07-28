/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.syncbus.intf.ICompleteOffer;
import com.triniforce.syncbus.intf.ICompletePublisher;

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
            final ICompletePublisher eb = ctx.mock(ICompletePublisher.class);
            
            ctx.checking(new Expectations(){{
                exactly(1).of(eb).setIEntireOffer(with(any(ICompleteOffer.class)));
            }});
            
            sb.registerEntirePub(eb);
            
            ctx.assertIsSatisfied();
            
        }        
    }
    

}
