/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.InSeparateThreadExecutor.ExecutionResult;

public class InSeparateThreadExecutorTest extends TFTestCase {
    
    int value;
    
    @Override
    public void test() throws Exception {
        
        //normal thread
        {
            IRunnable r = new IRunnable(){
                public void run() {
                    value = 314;
                }
            };
            InSeparateThreadExecutor e = new InSeparateThreadExecutor();
            ExecutionResult res =e.execute("", r, 10000L);
            assertFalse(res.timeoutExpired);
            assertNull(res.exception);
            assertEquals(314, value);
        }
        
        //timeout
        {
            IRunnable r = new IRunnable(){
                public void run() {
                    ICheckInterrupted.Helper.sleep(50000);
                }
            };
            InSeparateThreadExecutor ex = new InSeparateThreadExecutor();
            try{
                ex.execute("MyTask-timeout", r, 1000L);
            }catch(InSeparateThreadExecutor.EExecutorTimeoutExpired e){
                trace(e);
                assertTrue(e.getMessage().contains("MyTask-timeout"));
            }
            assertTrue(ex.getResult().timeoutExpired);
            ex.getResult().t.join(2000);
            assertFalse(ex.getResult().t.isAlive());
        }
        
        //exception
        {
            IRunnable r = new IRunnable(){
                public void run() {
                    throw new EUtils.EAssertNotNullFailed("6789123"); 
                }
            };
            InSeparateThreadExecutor ex = new InSeparateThreadExecutor();
            try{
                ex.execute("", r, 10000L);
                fail();
            }catch(EUtils.EAssertNotNullFailed e){
                trace(e);
            }
        }        

    }
    

}
