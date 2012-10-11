/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.test.TFTestCase;

public class DBOSortTest extends TFTestCase {

	@Override
	public void test() throws Exception {
		DBOSort s = new DBOSort();
		{
			ArrayList<IDBObject> list = new ArrayList<IDBObject>();
			IDBObject obj = dbo();
			list.add(dbo());
			list.add(dbo(obj));
			list.add(obj);
			List<IDBObject> res = s.sort(list);
			
			assertEquals(res.size(), list.size());
			assertSame(obj, res.get(1));
		}
		{
			ArrayList<IDBObject> list = new ArrayList<IDBObject>();
			IDBObject obj = dbo();
			IDBObject obj2 = dbo(obj);
			IDBObject obj3 = dbo(obj2);
			list.add(obj3);
			list.add(obj);
			list.add(obj2);
			
			List<IDBObject> res = s.sort(list);
			assertSame(obj,  res.get(0));
			assertSame(obj2, res.get(1));
			assertSame(obj3, res.get(2));
			
		}
	}

	private IDBObject dbo(final IDBObject...deps) {
		return new IDBObject() {
			
			public IDBObject[] synthDBObjects() {
				// TODO Auto-generated method stub
				return null;
			}
			
			public Object getKey() {
				// TODO Auto-generated method stub
				return null;
			}
			
			public IDBObject[] getDependiencies() {
				return deps;
			}
			
			public Class getActualizerClass() {
				return null;
			}
		};
	}
}
