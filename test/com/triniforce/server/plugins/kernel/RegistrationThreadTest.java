/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.plugins.kernel.BasicServer.RegistrationThread;
import com.triniforce.server.srvapi.ISORegistration;

public class RegistrationThreadTest extends BasicServerTestCase {
	
	boolean bDone = false;
	
	@Override
	protected void setUp() throws Exception {
		addPlugin(new PKPlugin() {
			
			@Override
			public void doRegistration(ISORegistration reg) throws EDBObjectException {
				if(!bDone)
					bDone = true;
			}
			
			@Override
			public void doExtensionPointsRegistration() {
				
			}

			@Override
			public void doRegistration() {
				// TODO Auto-generated method stub
				
			}
		});
		super.setUp();
	}
	
	int i = 2;

	public void test(){
		bDone = false;
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
			}
		};
		RegistrationThread t = new BasicServer.RegistrationThread(getServer(), m_coreApi, r);
		t.setSrvRegistered(true);
		t.run();
		
		assertFalse(bDone);
	}
}
