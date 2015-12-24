/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.triniforce.db.ddl.ActualStateBL.TIndexNames;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;

public class MDSDbTest extends BasicServerTestCase{

    public void testCreateFromResSet() throws SQLException {
    	
    	//
    	MDS mds = MDSDb.createFromResSet(null);
        assertNull(mds);
    	//
        getServer().enterMode(Mode.Running);
        try{
            ISrvSmartTran  st = ApiStack.getInterface(ISrvSmartTran.class);
            ResSet rs = st.select(TIndexNames.class, new IName[]{TIndexNames.appName, TIndexNames.dbName}, new IName[]{}, new IName[]{});
                        
            mds = MDSDb.createFromResSet(rs);
            assertNotNull(mds);
            
            ResultSetMetaData md = rs.getResultSet().getMetaData();
            
            assertEquals(mds.getNamesMap().size(),md.getColumnCount());
            for (int i = 0; i < md.getColumnCount(); i++) {
            	String rsCol = md.getColumnName(i+1);
            	Integer index = mds.getNamesMap().get(rsCol);
            	assertNotNull(index);
            	assertEquals(index.intValue(),i);
			}
            
            ResSet rs2 = st.select(TIndexNames.class, new IName[]{TIndexNames.appName, TIndexNames.dbName}, new IName[]{}, new IName[]{});
            
            int size = 0;
            int k = 0;
            while (rs2.next()) {
            	size = size + 1;
            	
            	String rs_appName = rs2.getString(1);
            	String rs_dbName = rs2.getString(2);
            	
            	int index1 = mds.getNamesMap().get(rs2.getResultSet().getMetaData().getColumnName(1)); 
            	int index2 = mds.getNamesMap().get(rs2.getResultSet().getMetaData().getColumnName(2)); 
            	for (IMDSRow row : mds) {
					
            		String mds_appName = (String)row.get(index1);
					if (rs_appName.equals(mds_appName)) {
						String mds_dbName = (String)row.get(index2);
						assertEquals(rs_dbName, mds_dbName);
						k = k + 1;
					}
            	}
            }
            assertEquals(mds.getRows().size(),size);
            assertEquals(k,size);
            
            rs.getResultSet().close();
            rs2.getResultSet().close();
            st.close();
            
            
        }finally{
        	getServer().leaveMode();
        }
        
        
    }

}
