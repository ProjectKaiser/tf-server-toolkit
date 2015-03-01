/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.test;

import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.utils.ApiStack;

public class BasicServerTestCaseInv extends BasicServerTestCase{
    @Override
    public void test() throws Exception {
        getServer().enterMode(Mode.Running);
        try{
            IPooledConnection con = ApiStack.getInterface(IPooledConnection.class);
            con.getPooledConnection();
        }finally{
            getServer().leaveMode();
        }
    }

}
