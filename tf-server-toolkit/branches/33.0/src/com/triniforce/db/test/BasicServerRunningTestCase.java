/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.test;

import com.triniforce.server.srvapi.IBasicServer.Mode;

public class BasicServerRunningTestCase extends BasicServerTestCase{
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_server.enterMode(Mode.Running);
    }
    @Override
    protected void tearDown() throws Exception {
        m_server.leaveMode();
        super.tearDown();
    }

}
