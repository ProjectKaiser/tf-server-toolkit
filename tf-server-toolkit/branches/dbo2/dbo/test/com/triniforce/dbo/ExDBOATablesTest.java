/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import java.util.Arrays;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class ExDBOATablesTest extends BasicServerTestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		getServer().enterMode(Mode.Registration);
	}
	
	@Override
	protected void tearDown() throws Exception {
		getServer().leaveMode();
		super.tearDown();
	}
	
	@Override
	public void test() throws Exception {
		IPKRootExtensionPoint rootEP = ApiStack.getInterface(IBasicServer.class);
		IPKExtensionPoint ep = rootEP.getExtensionPoint(PKEPDBOActualizers.class);
		ExDBOATables act = ep.getExtension(ExDBOATables.class).getInstance();
		TableDef td = new TableDef("ExDBOATablesTest_01");
		td.addScalarField(1, "f_01", ColumnType.INT, true, 0);
		act.actualize(Arrays.asList((IDBObject)new DBOTabDef(td)));
		
		ApiStack.getInterface(ISOQuery.class).getEntity("ExDBOATablesTest_01");
	}
}
