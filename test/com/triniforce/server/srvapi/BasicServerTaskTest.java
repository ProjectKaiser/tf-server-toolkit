/* 
 * Copyright(C) Triniforce
 * All Rights Reserved.
 * 
 */
package com.triniforce.server.srvapi;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IBasicServer.Mode;

public class BasicServerTaskTest extends BasicServerTestCase {

	
	public void test(){
		getServer().enterMode(Mode.Running);
		try{
			BasicServerTask t = new BasicServerTask() {
				
				@Override
				public void run() {
				}
			};
			
			t.init();
			t.finit();
			t.init();
			t.finit();
		}finally{
			getServer().leaveMode();
		}
	}
}
