/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;

import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.utils.ApiAlgs;

public class TFPooledConnection implements IPooledConnection{

    Map<Connection, StackTraceElement[]> m_conStack = new HashMap<Connection, StackTraceElement[]>();

    public BasicDataSource m_ds = null;

    public TFPooledConnection(BasicDataSource ds, int maxActive){
        m_ds = ds;
        m_ds.setMaxTotal(maxActive);
    }

    public Connection getPooledConnection() throws SQLException {
    	synchronized (this){
	        if( m_ds.getNumActive()  >= m_ds.getMaxTotal() - 2 ){
	            for (StackTraceElement[] trace : m_conStack.values()) {
	                String s = "Pooled Connection Exhausted: ";
	                for (StackTraceElement tr : trace) {
	                    s = s + tr.toString() + "\n";
	                }
	                ApiAlgs.getLog(this).info(s);
	            }
	        }
    	}
        Connection con = m_ds.getConnection();
        con.setAutoCommit(false);
        synchronized (this) {
        	m_conStack.put(con, Thread.currentThread().getStackTrace());            	
		}
        return con;
    }

    public void returnConnection(Connection con) throws SQLException {
    	synchronized (this) {
            m_conStack.remove(con);
		}
        con.close();
    }

    public int getMaxIdle() {
        return m_ds.getMaxIdle();
    }

    public int getNumIdle() {
        return m_ds.getNumIdle();
    }

    public void setMaxIdle(int maxIdle) {
        m_ds.setMaxIdle(maxIdle);
    }

	@Override
	public void close() {
		try {
			m_ds.close();
		} catch (SQLException e) {
			ApiAlgs.rethrowException(e);
		}
	}
	
	@Override
	public String getInfo() {
		
		String s = "";
		int i = 0;
		for (StackTraceElement[] trace : m_conStack.values()) {
            i =i + 1;
            s = s + "---- " + i + " ----" + "\n";
            for (StackTraceElement tr : trace) {
                s = s + tr.toString() + "\n";
            }
        }
		s = "MaxActive = " + m_ds.getMaxTotal() + "\n" + 
            "NumActive = " + m_ds.getNumActive() + "\n" +
            "MaxWaitMillis = "   + m_ds.getMaxWaitMillis()   + "\n" +	
            "Total in conStack: " + i +  "\n" + s + "----" + "\n"; 
		
		return s;
	}	
    
}
