/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.IPooledConnection.StackTraceRec;
import com.triniforce.server.srvapi.ISrvPrepSqlGetter;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class SrvSmartTranFactory implements ISrvSmartTranFactory {
	
	public static class EPoolConnectionError extends RuntimeException {
		private Collection<StackTraceRec> m_points;
		int MaxActive, NumActive;
		long MaxWaitMillis;

		public EPoolConnectionError(SQLException e, String info, Collection<StackTraceRec> takenConnectionPoints, int MaxActive, int NumActive, long MaxWaitMillis) {
			super(info, e);
			m_points = takenConnectionPoints;
			this.MaxActive = MaxActive;
			this.NumActive = NumActive;
			this.MaxWaitMillis = MaxWaitMillis;
		}
		
		public Collection<StackTraceRec> getPoints(){
			return m_points;
		}

		private static final long serialVersionUID = 943914984027051866L;
		
		public int getMaxActive() {
			return MaxActive;
		}
		
		public int getNumActive() {
			return NumActive;
		}
		
		public long getMaxWaitMillis() {
			return MaxWaitMillis;
		}
	}

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
        	int MaxActive=0;
        	int NumActive=0; 
        	long MaxWaitMillis=0;
        	if(pool instanceof TFPooledConnection) {
        		BasicDataSource ds = ((TFPooledConnection)pool).m_ds;
        		MaxActive = ds.getMaxTotal(); 
        		NumActive = ds.getNumActive();
        		MaxWaitMillis = ds.getMaxWaitMillis();
        		
        	}
        	
        	throw new EPoolConnectionError(e, pool.getInfo(), pool.getTakenConnectionPoints(), MaxActive, NumActive, MaxWaitMillis);        	
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
