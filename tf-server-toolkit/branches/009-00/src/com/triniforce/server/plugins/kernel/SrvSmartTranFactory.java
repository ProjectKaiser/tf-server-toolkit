/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISrvPrepSqlGetter;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class SrvSmartTranFactory implements ISrvSmartTranFactory {

    private List<ITranExtender> m_outterExtenders = new ArrayList<ITranExtender>();
    private List<ITranExtender> m_innerExtenders = new ArrayList<ITranExtender>();

	public void pop() {
        try {
            ISrvSmartTran trn = SrvApiAlgs2.getIServerTran();

            trn.close();
            
            IPooledConnection pool = ApiStack.getApi().getIntfImplementor(
                    IPooledConnection.class);
            Connection con = ApiStack.getApi().getIntfImplementor(
                    Connection.class);
            try {
                pool.returnConnection(con);
            } catch (Throwable t) {
                ApiAlgs.getLog(this).error("Error returning connection", t);
            }
            
        } finally {
            ApiStack.popApi();
        }
    }

    public void push() {
    	
    	ISrvPrepSqlGetter sqlGetter = (ISrvPrepSqlGetter) ApiStack
    		.queryInterface(ISrvPrepSqlGetter.class);
        IPooledConnection pool = ApiStack.getApi().getIntfImplementor(
                IPooledConnection.class);
        try {
            Connection con = pool.getPooledConnection();
            Api api = new Api();
            api.setIntfImplementor(Connection.class, con);
            ApiStack.pushApi(api);
            api.setIntfImplementor(ISrvSmartTran.class, new SrvSmartTran(con, sqlGetter)); 
        } catch (SQLException e) {
            ApiAlgs.rethrowException(e);
        }
    }

	public void registerInnerExtender(ITranExtender interceptor) {
		m_innerExtenders.add(interceptor);
	}

	public void registerOuterExtender(ITranExtender interceptor) {
		m_outterExtenders.add(interceptor);
	}

	public List<ITranExtender> getInnerExtenders() {
		return m_innerExtenders;
	}

	public List<ITranExtender> getOuterExtenders() {
		return m_outterExtenders;
	}

}
