/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import java.util.Arrays;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.utils.ApiStack;

public class ExDBOAUpgradeProceduresTest extends BasicServerTestCase {
	
	@Override
	protected void setUp() throws Exception {
		restartServerOnSetup();
		super.setUp();
		getServer().enterMode(Mode.Upgrade);
		ISrvSmartTranFactory trnFact = ApiStack.getApi()
		.getIntfImplementor(ISrvSmartTranFactory.class);
		trnFact.push();

	}
	
	@Override
	protected void tearDown() throws Exception {
		ISrvSmartTranFactory trnFact = ApiStack.getApi()
		.getIntfImplementor(ISrvSmartTranFactory.class);
		trnFact.pop();
		getServer().leaveMode();
		super.tearDown();
	}
	
	static boolean bCall = false; 
	
	static class TestProc extends UpgradeProcedure{
		public TestProc() {
		}
		
		@Override
		public void run() throws Exception {
			bCall = true;			
		}
	}
	
	@Override
	public void test() throws Exception {
		IPKRootExtensionPoint rootEP = ApiStack.getInterface(IBasicServer.class);
		IPKExtensionPoint ep = rootEP.getExtensionPoint(PKEPDBOActualizers.class);
		ExDBOAUpgradeProcedures act = ep.getExtension(ExDBOAUpgradeProcedures.class).getInstance();
		
		act.actualize(Arrays.asList((IDBObject)new DBOUpgProcedure(new TestProc())));
		assertTrue(bCall);
		
		bCall = false;
		act.actualize(Arrays.asList((IDBObject)new DBOUpgProcedure(new TestProc())));
		assertFalse(bCall);
		
		
	}
}
