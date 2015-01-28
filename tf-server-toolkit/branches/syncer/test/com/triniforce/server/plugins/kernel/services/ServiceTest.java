/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.services;

import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class ServiceTest extends ServicesTestCase {
   
    public static void restartTran(boolean bCommit) {
        ISrvSmartTranFactory tf = ApiStack.getApi().getIntfImplementor(
                ISrvSmartTranFactory.class);
        if (bCommit) {
            ISrvSmartTran tran = ApiStack.getApi().getIntfImplementor(
                    ISrvSmartTran.class);
            tran.commit();
        }
        tf.pop();
        tf.push();
    }    
    
    class MyCT1 extends Service {

        @Override
        public int getCyclePauseMs() {
            return 100;
        }

        @Override
        public void doCycle() {
//            ICurrentUser cu = ApiStack.getInterface(ICurrentUser.class);
//            assertEquals(ReservedKeys.SYSTEM.getKey(), cu.getCurrentUserID());
//            assertEquals(ReservedKeys.SYSTEM.getKey(), cu.getCurrentUserSID());
            ApiAlgs.getLog(this).trace("go");
        }

        @Override
        public void finit() {
            ApiAlgs.getLog(this).trace("bye");
            super.finit();
        }

    }

    class MyCTErrInCycle extends Service {

        @Override
        public int getCyclePauseMs() {
            return 100;
        }

        @Override
        public void doCycle() {
            ApiAlgs.assertTrue(false, "oops");
        }

        @Override
        public void finit() {
            ApiAlgs.getLog(this).trace("bye");
            super.finit();
        }

        @Override
        public int getCycleExceptionPauseMs() {
            return 100;
        }

    }
    
    public void test() throws Exception {
        {// simple
            Service ct = new MyCT1();
            assertEquals(IService.State.STOPPED, ct.getState());
            ct.start();
            assertEquals(IService.State.RUNNING, ct.getState());            
            Thread.sleep(1000);
            ct.stop();
            assertEquals(IService.State.STOPPED, ct.getState());            
        }
/*        {// MyCTErrInCycle
            Service ct = new MyCTErrInCycle();
            assertEquals(Service.State.STOPPED, ct.getState());
            ct.start(m_server);
            Thread.sleep(1000);
            ct.stop();
            ct.join();
        }*/
    }

}
