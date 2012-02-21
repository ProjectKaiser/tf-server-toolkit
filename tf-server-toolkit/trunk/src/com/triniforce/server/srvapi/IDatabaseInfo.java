/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.srvapi;

import com.triniforce.db.ddl.UpgradeRunner.DbType;

public interface IDatabaseInfo {

	DbType getDbType();
	String getIdentifierQuoteString();
}
