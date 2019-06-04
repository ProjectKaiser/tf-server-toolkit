/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.test.TFTestCase;

public class TFPooledConnectionTest extends TFTestCase {

	
	public void test() throws Exception {
		TFPooledConnection pool = new TFPooledConnection(getDataSource(), 10);
		
		for(int i=0; i< 12; i++){
			pool.getPooledConnection();
		}
	}
}
