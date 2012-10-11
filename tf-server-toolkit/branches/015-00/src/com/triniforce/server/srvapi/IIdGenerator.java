/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import java.sql.SQLException;

public interface IIdGenerator extends IModeUpgrade, IModeRunning {
    /**
     *  @return new key value. The lowest possible value is defined by SrvApiConsts.MIN_GENERATED_KEY
     * @throws SQLException 
     */
    long getKey();
}
