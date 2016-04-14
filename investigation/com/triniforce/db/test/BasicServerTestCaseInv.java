/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.test;

import java.sql.Connection;

import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.utils.ApiStack;

public class BasicServerTestCaseInv extends BasicServerTestCase{
    @Override
    public void test() throws Exception {
        getServer().enterMode(Mode.Running);
        try{
            IPooledConnection con = ApiStack.getInterface(IPooledConnection.class);
            Connection c1 = con.getPooledConnection();
            con.returnConnection(c1);
        }finally{
            getServer().leaveMode();
        }
    }

}
