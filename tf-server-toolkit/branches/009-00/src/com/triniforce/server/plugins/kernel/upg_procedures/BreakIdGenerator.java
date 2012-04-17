/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.upg_procedures;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.triniforce.server.plugins.kernel.BasicServer;
import com.triniforce.server.plugins.kernel.IdGenerator;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class BreakIdGenerator  extends UpgradeProcedure {

	private IdGenerator[] m_gens;

	public BreakIdGenerator(IdGenerator... gens) {
        super("Break id-generators");
        m_gens = gens;
    }

    @Override
    public void run() throws Exception {
        Connection con = SrvApiAlgs2.getPooledConnection();
        try {
            ISODbInfo dbInfo = ApiStack.getInterface(ISODbInfo.class);
            if (isNewDb(con, dbInfo.getTableDbName(BasicServer.DPP_TABLE)))
                return;
            runAlways(con);
        } finally {
            SrvApiAlgs2.returnPooledConnection(ApiStack.getApi(), con);
        }
    }
    
    public void runAlways(Connection con) throws Exception {
    	long v = ApiStack.getInterface(IIdGenerator.class).getKey();
    	for(IdGenerator gen : m_gens)
    		gen.setKey(v);
    }
    
    boolean isNewDb(Connection con, String dppTab){
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select count(*) from " + dppTab);
            try{
                ApiAlgs.assertTrue(rs.next(),"");
                return 0 == rs.getInt(1);
            }finally{
                rs.close();
            }
        } catch (SQLException e) {
            ApiAlgs.rethrowException(e);
            return false;
        }        
    }
}
