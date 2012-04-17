/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import java.util.Arrays;
import java.util.Map;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class PKEPDBObjectsTest extends BasicServerTestCase {
	
	static class TestDBO implements IDBObject{
		
		private Class m_actualizer;
		private IDBObject[] m_deps;
		private IDBObject[] m_synths;
		private Object m_key;

		public TestDBO(String key, Class actualizer, IDBObject[] deps, IDBObject[] synths) {
			m_actualizer = actualizer;
			m_deps = deps;
			m_synths = synths;
			m_key = key;
		}

		public Class getActualizerClass() {
			return m_actualizer;
		}

		public IDBObject[] getDependiencies() {
			return m_deps;
		}

		public IDBObject[] synthDBObjects() {
			return m_synths;
		}

		public Object getKey() {
			return m_key;
		}
		
	}
	
	@Override
	protected void setUp() throws Exception {
//		PKPlugin pl = new PKPlugin() {
//			@Override
//			public void doRegistration() {
//			}
//			
//			@Override
//			public void doExtensionPointsRegistration() {
//				putExtension(PKEPDBObjects.class, "TestDBO_01", new TestDBO("TestDBO_01", null, null, 
//						new IDBObject[]{
//							new TestDBO("TestDBO_01_01", null, null, new IDBObject[]{})
//				}));
//			}
//		};
//		addPlugin(pl);
		super.setUp();
		getServer().enterMode(Mode.Running);
	}
	@Override
	protected void tearDown() throws Exception {
		getServer().leaveMode();
		super.tearDown();
	}

	@Override
	public void test() throws Exception {
		IPKRootExtensionPoint rootEP = ApiStack.getInterface(IBasicServer.class);
		IPKExtensionPoint ep = rootEP.getExtensionPoint(PKEPDBObjects.class);
		ep.putExtension("TestDBO_01", new TestDBO("TestDBO_01", null, null, 
		new IDBObject[]{
			new TestDBO("TestDBO_01_01", null, null, new IDBObject[]{})
		}));
		Map<String, IPKExtension> map = ep.getExtensions();
		assertTrue(map.keySet().containsAll(Arrays.asList("TestDBO_01", "TestDBO_01_01")));
	}
}
