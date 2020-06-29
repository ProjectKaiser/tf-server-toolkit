/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import java.sql.Connection;
import java.sql.SQLException;

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
    String getInfo();
}    
