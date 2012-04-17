/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.BasicServerCorePlugin.RefCountMapTrnExtender;
import com.triniforce.server.plugins.kernel.BasicServerCorePlugin.RefCountMapTrnExtender.RefCountMap;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap.IFactory;

public class RefCountMapTest extends TFTestCase {
	
	public void testRefCountMap(){
		
		RefCountMap countMap = new RefCountMap(serverMap(
				"key1", 1, 672490623,
				"key2", 2, 672490624,
				"key3", 3, 672490625
		));
		
		assertEquals(672490624, countMap.getServerValue("key2"));
	}
	
	Map<Object, RefCountMapTrnExtender.CountedObject> serverMap(Object ...objects){
		HashMap<Object, RefCountMapTrnExtender.CountedObject> map = new HashMap<Object, RefCountMapTrnExtender.CountedObject>();
		for(int i=0 ; i < objects.length/3; i++){
			int off = i*3;
			RefCountMapTrnExtender.CountedObject obj = new RefCountMapTrnExtender.CountedObject(objects[off+2]);
			for(int j=0; j<(Integer)objects[off+1]; j++)
				obj.addRef();
			map.put(objects[off], obj);
		}
		return map;
	}

	public void testPut() {
		RefCountMap countMap = new RefCountMap(serverMap(
				"key15", 1, 943634L,
				"key16", 4, 943636L
		));
		Mockery ctx = new Mockery();
		final IFactory f = ctx.mock(IRefCountHashMap.IFactory.class);
		ctx.checking(new Expectations(){{
			one(f).newInstance("key_1"); will(returnValue(7230967309L));
			one(f).newInstance("key_2"); will(returnValue(7230967311L));
		}});
		Object v1 = countMap.put("key_1", f);
		assertEquals(Long.valueOf(7230967309L), v1);
		assertEquals(Long.valueOf(7230967311L), countMap.put("key_2", f));
		assertSame(v1, countMap.put("key_1", f));
		assertEquals(new HashSet<Object>(Arrays.asList("key_1","key_2")), countMap.getTransactionKeys());
		assertEquals(1, countMap.getServerRefCount("key_1"));
		
		assertEquals(943634L, countMap.put("key15", f));
		assertEquals(2, countMap.getServerRefCount("key15"));
		ctx.assertIsSatisfied();
	}

	public void testGetServerValue() {
		RefCountMap countMap = new RefCountMap(serverMap());
		Mockery ctx = new Mockery();
		final IFactory f = ctx.mock(IRefCountHashMap.IFactory.class);
		ctx.checking(new Expectations(){{
			one(f).newInstance(73237); will(returnValue("string_1"));
		}});
		assertNull(countMap.getServerValue(68423));
		assertNull(countMap.getServerValue(73237));
		
		countMap.put(73237, f);
		
		assertEquals("string_1", countMap.getServerValue(73237));
		
		ctx.assertIsSatisfied();
	}

	public void testGetServerRefCount() {
		RefCountMap countMap = new RefCountMap(serverMap(
				"key1", 1, 672490623,
				"key2", 2, 672490624,
				"key3", 3, 672490625
		));
		Mockery ctx = new Mockery();
		final IFactory f = ctx.mock(IRefCountHashMap.IFactory.class);
		ctx.checking(new Expectations(){{
			one(f).newInstance(777); will(returnValue("s_1"));
		}});
		assertEquals(0, countMap.getServerRefCount(777));
		countMap.put(777, f);
		countMap.put(777, f);
		assertEquals(1, countMap.getServerRefCount(777));
		
		assertEquals(2, countMap.getServerRefCount("key2"));
		assertEquals(3, countMap.getServerRefCount("key3"));
		
	}

	public void testGetTransactionKeys() {
		
		Map<Object, RefCountMapTrnExtender.CountedObject> map = serverMap("srv_key1", 1, 99502);
		RefCountMap countMap = new RefCountMap(map);
		Mockery ctx = new Mockery();
		final IFactory f = ctx.mock(IRefCountHashMap.IFactory.class);
		ctx.checking(new Expectations(){{
			one(f).newInstance("key_1"); will(returnValue(7230967309L));
			one(f).newInstance("key_2"); will(returnValue(7230967311L));
		}});
		countMap.put("key_1", f);
		countMap.put("key_2", f);
		
		assertEquals(new HashSet<Object>(Arrays.asList("key_1", "key_2")), countMap.getTransactionKeys());
		
	}

	public void testGetServerKeys() {
		RefCountMap countMap = new RefCountMap(serverMap(
				"srv_key1", 1, 99502,
				"srv_key2", 3, 99503,
				"srv_key3", 2, 99504
		));
		assertEquals(new HashSet<Object>(Arrays.asList("srv_key1", "srv_key2", "srv_key3")), 
				countMap.getServerKeys());
		
		Mockery ctx = new Mockery();
		final IFactory f = ctx.mock(IRefCountHashMap.IFactory.class);
		ctx.checking(new Expectations(){{
			one(f).newInstance("key_1"); will(returnValue(7230967309L));
			one(f).newInstance("key_2"); will(returnValue(7230967311L));
		}});
		countMap.put("key_1", f);
		countMap.put("key_2", f);
		
		assertEquals(new HashSet<Object>(Arrays.asList("srv_key1", "srv_key2", "srv_key3", "key_1", "key_2")), 
				countMap.getServerKeys());
		
	}

	public void testFlush() {
		RefCountMap countMap = new RefCountMap(serverMap(
				"srv_key1", 1, 99502,
				"srv_key2", 3, 99503,
				"srv_key3", 2, 99504
		));
		
		Mockery ctx = new Mockery();
		final IFactory f = ctx.mock(IRefCountHashMap.IFactory.class);
		ctx.checking(new Expectations(){{
			one(f).newInstance("key_1"); will(returnValue(7230967309L));
			one(f).newInstance("key_1"); will(returnValue(7230967309L));
		}});
		Object v1 = countMap.put("key_1", f);
		Object v2 = countMap.put("srv_key2", f);
		
		countMap.flush();
		
		assertEquals(3, countMap.getServerRefCount("srv_key2"));
		assertEquals(0, countMap.getServerRefCount("key_1"));
		
		assertEquals(Collections.emptySet(), countMap.getTransactionKeys());
		assertEquals(new HashSet<Object>(Arrays.asList("srv_key1", "srv_key2", "srv_key3")), 
				countMap.getServerKeys());
		
		assertSame(v2, countMap.put("srv_key2", f));
		assertNotSame(v1, countMap.put("key_1", f));
		
		ctx.assertIsSatisfied();
	}

	boolean bShutdown= false;
	
	public void testStress() throws InterruptedException{
		/*ArrayList<Thread> threads = new ArrayList<Thread>();
		for(int i=0; i<6; i++){
			threads.add(new Thread(){
					@Override
					public void run() {
						while(!bShutdown){
							
						}
					}
				});			
		}
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		Thread.sleep(2000);
		
		for (Thread thread : threads) {
			thread.join();
		}*/
	}

}
