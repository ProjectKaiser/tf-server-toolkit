/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.api;

import com.triniforce.db.test.TFTestCase;

public class PKEPAPIsTest extends TFTestCase {
    
    static int cnt = 0;
    
    class MyAPI1 implements IPKEPAPI{
        public Class getImplementedInterface() {
            return null;
        }        
    }
    class MyAPI2 implements IPKEPAPI, IFinitApi{
        int fcnt;
        public Class getImplementedInterface() {
            return null;
        }

        public void finitApi() {
            fcnt = cnt++;
            
        }        
    }

    
    @Override
    public void test() throws Exception {
        
        cnt = 111;
        
        PKEPAPIs apis = new PKEPAPIs();
        MyAPI1 api1 = new MyAPI1();
        MyAPI2 api2 = new MyAPI2();
        MyAPI2 api2_2 = new MyAPI2();
        apis.putExtension("api1", api1);
        apis.putExtension("api2", api2);
        apis.putExtension("api2_2", api2_2);
        apis.finit();
        //test that finit called in reverse order
        assertEquals(112, api2.fcnt);
        assertEquals(111, api2_2.fcnt);

    }
    

}
