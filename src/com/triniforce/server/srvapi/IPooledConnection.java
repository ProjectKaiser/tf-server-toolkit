/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Represents pooling connection. Normally this interface is used by server
 * core to install connection, so plugins should use IConnection interface
 *
 */
public interface IPooledConnection extends IModeAny{
    Connection getPooledConnection() throws SQLException;
    void returnConnection(Connection con) throws SQLException;
    
    int getMaxIdle();
    int getNumIdle();
    void setMaxIdle(int maxIdle);
    void close() throws SQLException;
    
	static class StackTraceRec{
		StackTraceElement[] m_trace;
		String m_info;
		public StackTraceRec(String info, StackTraceElement[] stackTrace) {
			m_info = info;
			m_trace = stackTrace;
				
		}
		public StackTraceElement[] getTrace() {
			return m_trace;
		}
		public String getInfo() {
			return m_info;
		}
	}
	
    
    String getInfo();
    
	
	Collection<StackTraceRec> getTakenConnectionPoints();
    
}    
