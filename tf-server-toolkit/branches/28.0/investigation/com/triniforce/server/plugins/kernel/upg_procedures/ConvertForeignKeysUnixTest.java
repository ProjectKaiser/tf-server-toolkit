/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.upg_procedures;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class ConvertForeignKeysUnixTest extends BasicServerTestCase{
    ISOQuery getISOQuery(){
        return ApiStack.getInterface(ISOQuery.class);
    }
    
    public void test() throws Exception{
        
        getServer().enterMode(Mode.Running);
        try{
        
        //checkForeignKeys(true);
        ConvertForeignKeys proc = new ConvertForeignKeys();
        proc.runAlways();
        
        ISrvSmartTranFactory.Helper.commit();

        checkForeignKeys(false);
        }finally{
            getServer().leaveMode();
        }
    }
    
    public void checkForeignKeys(boolean mustExist) throws Exception {
        Connection con = ApiStack.getInterface(Connection.class);
        DatabaseMetaData md = con.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);
        boolean found = false;
        while(rs.next()){
            ResultSet rsKeys = md.getExportedKeys(rs.getString(1), rs.getString(2), rs.getString(3));
            while(rsKeys.next()){
                trace(rsKeys.getString(3) + "." + rsKeys.getString(4) + "<="
                + rsKeys.getString(7) + "." + rsKeys.getString(8)        
                );  
                found = true;
            }
        }
        assertTrue("Foreign keys exist = NOT " +mustExist + ", see trace", (mustExist && found) || (!mustExist && !found));
    }


}
