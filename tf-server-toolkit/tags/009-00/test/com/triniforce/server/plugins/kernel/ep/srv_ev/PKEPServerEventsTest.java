/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.srv_ev;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IBasicServer;

public class PKEPServerEventsTest extends TFTestCase {

    List<Integer> m_ords = new ArrayList<Integer>();
    
    class MyHandler extends ServerEventHandler{
        private final Integer m_ord;
        public MyHandler(int ord) {
            m_ord = ord;
        }
        @Override
        public void handleEvent(IBasicServer srv, ServerEvent event) {
            m_ords.add(m_ord);
        }
    }
    
    @Override
    public void test() throws Exception {
        PKEPServerEvents ep = new PKEPServerEvents();
        ep.putExtension("id1", new MyHandler(1));
        ep.putExtension("id2", new MyHandler(2));

        //direct order
        {
            m_ords.clear();
            ep.handleEventDirectOrder(null, ServerEvent.PLUGINS_REGISTRATION_FINISHED);
            assertEquals(2, m_ords.size());
            assertEquals((Integer)1, m_ords.get(0));
            assertEquals((Integer)2, m_ords.get(1));
        }
        //reverse order
        {
            m_ords.clear();
            ep.handleEventReverseOrder(null, ServerEvent.PLUGINS_REGISTRATION_FINISHED);
            assertEquals(2, m_ords.size());
            assertEquals((Integer)2, m_ords.get(0));
            assertEquals((Integer)1, m_ords.get(1));
        }        
        
    }
}
