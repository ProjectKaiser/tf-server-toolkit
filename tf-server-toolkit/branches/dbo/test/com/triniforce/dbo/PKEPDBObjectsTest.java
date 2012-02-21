/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.PKPlugin;

public class PKEPDBObjectsTest extends BasicServerTestCase {
	
	static class TestDBO{
		
	}
	
	@Override
	protected void setUp() throws Exception {
		PKPlugin pl = new PKPlugin() {
			@Override
			public void doRegistration() {
			}
			
			@Override
			public void doExtensionPointsRegistration() {
				putExtension(PKEPDBObjects.class, TestDBO.class);
			}
		};
		addPlugin(pl);
		super.setUp();
	}

	@Override
	public void test() throws Exception {
		
	}
}
