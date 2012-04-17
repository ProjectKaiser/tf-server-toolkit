/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.util.List;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.dbo.DBOActualizer;
import com.triniforce.dbo.IDBObject;
import com.triniforce.dbo.PKEPDBOActualizers;
import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.srvapi.IBasicServer.Mode;

public class BasicServerPKEPActualizerTest extends BasicServerTestCase {
	
	static List<IDBObject> list1, list2;
	
	public static class TestAct extends DBOActualizer{

		public TestAct() {
			super(true, Mode.Upgrade);
		}
		
		
		@Override
		public void actualize(List<IDBObject> dboList) {
			super.actualize(dboList);
			list1 = dboList;
		}
		
	}
	public static class TestAct2 extends DBOActualizer{

		public TestAct2() {
			super(true, Mode.Upgrade);
		}
		
		@Override
		public void actualize(List<IDBObject> dboList) {
			super.actualize(dboList);
			list2 = dboList;
		}
		
	}
	
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
		PKPlugin pl = new PKPlugin() {
			@Override
			public void doRegistration() {
			}
			
			@Override
			public void doExtensionPointsRegistration() {
				putExtension(PKEPDBOActualizers.class, TestAct.class);
				putExtension(PKEPDBOActualizers.class, TestAct2.class);
				putExtension(PKEPDBObjects.class, "TestDBO_01", new TestDBO("TestDBO_01", TestAct.class, null, new IDBObject[]{}));
			}
		};
		addPlugin(pl);
		super.setUp();
		getServer().enterMode(Mode.Running);
	}
	
	@Override
	protected void tearDown() throws Exception {
		getServer().leaveMode();
		super.tearDown();
	}
	
	public void test() throws Exception {
		// Actualizers actualized lists
		assertNotNull(list1);
		assertNotNull(list2);
		
		assertTrue(list2.isEmpty());
		assertEquals("TestDBO_01", list1.get(0).getKey());
		
	}

}
