/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.upg_procedures;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Random;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiStack;

public class ConvertForeignKeysTest extends BasicServerTestCase {
    
    public static class TestTabDef1 extends TableDef{
        public TestTabDef1() {
            addScalarField(1, "id", ColumnType.INT, true, null);
            addPrimaryKey(2, "pk", new String[]{"id"});
        }
    } 

    public static class TestTabDef2 extends TableDef{
        public TestTabDef2(String tabName) {
            super(tabName);
            setSupportForeignKeys(true);
            addScalarField(1, "id", ColumnType.INT, true, null);
            addForeignKey(2, "fk", new String[]{"id"}, TestTabDef1.class.getName(), "pk", false);
        }
    } 

    ISOQuery getISOQuery(){
        return ApiStack.getInterface(ISOQuery.class);
    }
    
    public void test() throws Exception{
        
        getServer().enterMode(Mode.Running);
        try{
        
        getServer().updateTableDef((TableDef) getISOQuery().quieryEntity(TestTabDef1.class.getName()), new TestTabDef1());
        String rndName = "com.triniforce.server.plugins.kernel.procedures.TestTabDef_"+new Random().nextInt(10000);
        getServer().updateTableDef((TableDef) getISOQuery().quieryEntity(rndName), new TestTabDef2(rndName));
        
        checkForeignKeys(true);
        ConvertForeignKeys proc = new ConvertForeignKeys();
        proc.runAlways();
        
        ISrvSmartTranFactory.Helper.commit();

        checkForeignKeys(false);
        }finally{
            getServer().leaveMode();
        }
    }
    
    public void checkForeignKeys(boolean mustExist) throws Exception {
//    	ISOQuery soQ = ApiStack.getInterface(ISOQuery.class);
    	ISODbInfo soDbInfo = ApiStack.getInterface(ISODbInfo.class);
    	trace(soDbInfo.getDbTableNames());
    	
        Connection con = ApiStack.getInterface(Connection.class);
        DatabaseMetaData md = con.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);
        boolean found = false;
        while(rs.next()){
        	String tabDbName = rs.getString(3);
        	if(soDbInfo.getDbTableNames().contains(tabDbName.toLowerCase())){
	            ResultSet rsKeys = md.getExportedKeys(rs.getString(1), rs.getString(2), rs.getString(3));
	            while(rsKeys.next()){
	            	trace(rsKeys.getString(3) + "." + rsKeys.getString(4) + "<="
	                + rsKeys.getString(7) + "." + rsKeys.getString(8)        
	                );  
	                found = true;
	            }
        	}
        }
        assertTrue("Foreign keys exist = NOT " +mustExist + ", see trace", (mustExist && found) || (!mustExist && !found));
    }

}
