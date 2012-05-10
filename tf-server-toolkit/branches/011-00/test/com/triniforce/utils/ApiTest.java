/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.utils;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;
import com.triniforce.utils.IApi.EInterfaceNotFound;

import junit.framework.TestCase;

interface ITm {
    long currentTimeMillis();
}

interface ITm2 {
    long currentTimeMillis2();
}

public class ApiTest extends TestCase implements ITm {
    
    static class SrvApiThread extends Thread {
        
        public static ApiStack m_SrvApi;
        
        @Override
        public void run() {
            m_SrvApi = ApiStack.getThreadApiContainer();
        }
    }
    
    Api m_srvApi0;
    protected Api m_srvApi1;

    protected void setUp() throws Exception {
        super.setUp();
        m_srvApi0 = new Api();
        m_srvApi1 = new Api();
    }

    public final void testGetIntfImplementor() throws Exception {

        // no interface
        try {
            m_srvApi1.getIntfImplementor(ITm.class);
            assertTrue(false);
        } catch (EInterfaceNotFound e) {
        }

        // set api0.ITime
        {
            m_srvApi0.setIntfImplementor(ITm.class, (ITm) this);
            ITm it = m_srvApi0.getIntfImplementor(ITm.class);
            assertEquals(it.currentTimeMillis(), 12);
        }

        // set api1.ITm, currentTime= 22
        {
            m_srvApi1.setIntfImplementor(ITm.class, new ITm() {
                public long currentTimeMillis() {
                    return 22;
                }
            });
            ITm it = m_srvApi1.getIntfImplementor(ITm.class);
            assertEquals(it.currentTimeMillis(), 22);
        }

        // check cast error
        {
            {// ITm is ok
                ITm it = m_srvApi1.getIntfImplementor(ITm.class);
                assertEquals(it.currentTimeMillis(), 22);
            }
            try {// ITm2 not
                ITm2 it = m_srvApi1.getIntfImplementor(ITm.class);
                assertTrue(false);// must be thrown before
                assertEquals(it.currentTimeMillis2(), 22);
            } catch (ClassCastException e) {
            }
        }
    }

    public final void testThreadApiContainer() throws Exception {
        
        {// nothing is installed yet
            assertNotNull(ApiStack.getApi());
            assertNotNull(ApiStack.getThreadApiContainer());
        }
        {// set m_srvApi0
            assertNull(ApiStack.getApi().queryIntfImplementor(ITm.class));
            ApiStack.pushApi(m_srvApi0);
            m_srvApi0.setIntfImplementor(ITm.class, new ITm(){
                public long currentTimeMillis() {
                    return 666L;
                }});
            ITm tm = ApiStack.getApi().getIntfImplementor(ITm.class);
            assertEquals(666L, tm.currentTimeMillis());
            
            ApiStack.pushApi(new Api(ITm2.class, new ITm2(){
                public long currentTimeMillis2() {
                    return 76489L;
                }}));
            
            assertEquals(666L, ((ITm)ApiStack.getApi().getIntfImplementor(ITm.class)).currentTimeMillis());
            assertEquals(76489L, ((ITm2)ApiStack.getApi().getIntfImplementor(ITm2.class)).currentTimeMillis2());
            
            ApiStack.popApi();
            try{
                ApiStack.getApi().getIntfImplementor(ITm2.class);
                fail();
            } catch(EInterfaceNotFound e){}
            
            ApiStack.pushApi(new Api(ITm.class, new ITm(){
                public long currentTimeMillis() {
                    return 76489L;
                }}));
            assertEquals(76489L, ((ITm)ApiStack.getApi().getIntfImplementor(ITm.class)).currentTimeMillis());            
        }

    }

    public final void testQueryIntfImplementor() {

        // api1.ITime = null
        assertNull(m_srvApi1.queryIntfImplementor(ITime.class));

        // set api0.ITime
        {
            m_srvApi0.setIntfImplementor(ITm.class, (ITm) this);
            ITm it = m_srvApi0.queryIntfImplementor(ITm.class);
            assertNotNull(it);
            assertEquals(it.currentTimeMillis(), 12);
        }

        // clear m_srvApi0 time
        m_srvApi0.clearIntfImplementor(ITm.class);
        assertNull(m_srvApi1.queryIntfImplementor(ITm.class));

        // set api1.ITm, currentTime= 22
        {
            m_srvApi1.setIntfImplementor(ITm.class, new ITm() {
                public long currentTimeMillis() {
                    return 22;
                }
            });
            ITm it = m_srvApi1.queryIntfImplementor(ITm.class);
            assertNotNull(it);
            assertEquals(it.currentTimeMillis(), 22);
        }

        // set api0.ITm, currentTime still 22
        {
            m_srvApi0.setIntfImplementor(ITm.class, (ITm) this);
            ITm it = m_srvApi1.queryIntfImplementor(ITm.class);
            assertNotNull(it);
            assertEquals(it.currentTimeMillis(), 22);
        }

        // clear api1.ITm, currentTime= 12
        {
            m_srvApi1.clearIntfImplementor(ITm.class);
//            assertEquals(((ITm) m_srvApi1.queryIntfImplementor(ITm.class))
//                    .currentTimeMillis(), 12);
          assertNull(((ITm) m_srvApi1.queryIntfImplementor(ITm.class)));
        }

        // set api0.ITm2
        {
            m_srvApi0.setIntfImplementor(ITm2.class, new ITm2() {
                public long currentTimeMillis2() {
                    return 333;
                }
            });
            ITm2 it = m_srvApi0.queryIntfImplementor(ITm2.class);
            assertNotNull(it);
            assertEquals(it.currentTimeMillis2(), 333);
        }

        // set api1.ITm2
        {
            m_srvApi1.setIntfImplementor(ITm2.class, new ITm2() {
                public long currentTimeMillis2() {
                    return 444;
                }
            });
            ITm2 it = m_srvApi1.queryIntfImplementor(ITm2.class);
            assertNotNull(it);
            assertEquals(it.currentTimeMillis2(), 444);
        }

    }

    public long currentTimeMillis() {
        return 12;
    }
    
    
    public void testFinit(){
    	Api api = new Api();
    	Mockery ctx = new Mockery();
    	final IFinitable fin = ctx.mock(IFinitable.class);
    	api.setIntfImplementor(String.class, fin);
    	api.setIntfImplementor(Long.class, fin);
    	api.setIntfImplementor(Integer.class, fin);
    	
    	ctx.checking(new Expectations(){{
    		exactly(3).of(fin).finit();
    	}});
    	
    	api.finit();
    	
    	ctx.assertIsSatisfied();
    	
    	ApiStack st = new ApiStack();
    	st.getStack().push(api);
    	Api api2 = new Api();
    	api2.setIntfImplementor(String.class, fin);
    	st.getStack().push(api2);

    	ctx.checking(new Expectations(){{
    		exactly(4).of(fin).finit();
    	}});
    	st.finit();
    	
    	ctx.assertIsSatisfied();

    }

}
