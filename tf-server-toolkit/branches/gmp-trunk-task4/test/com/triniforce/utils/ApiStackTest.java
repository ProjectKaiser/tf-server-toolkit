/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.utils;

import com.triniforce.db.test.TFTestCase;

public class ApiStackTest extends TFTestCase {
	
	public static interface MyIntf1{};
	public static interface MyIntf2{};
	public static interface MyIntf3{};
	
	public static class MyImplementor1{};
	public static class MyImplementor2{};
	public static class MyImplementor3{};
	
	public void test(){
		ApiStack as = new ApiStack();
		assertEquals(0, as.getImplementors().size());
		
		Api api1 = new Api();
		api1.setIntfImplementor(MyIntf2.class, new MyImplementor1());
		api1.setIntfImplementor(MyIntf1.class, new MyImplementor2());
		as.getStack().push(api1);
		assertEquals(2, as.getImplementors().size());
		
		assertTrue( as.getImplementors().containsKey(MyIntf1.class));
		assertTrue( as.getImplementors().containsKey(MyIntf2.class));
		assertFalse(as.getImplementors().containsKey(MyIntf3.class));
		
		Api api2 = new Api();
		api2.setIntfImplementor(MyIntf3.class, new MyImplementor3());
		as.getStack().push(api2);
		assertEquals(3, as.getImplementors().size());
		assertTrue( as.getImplementors().containsKey(MyIntf1.class));
		assertTrue( as.getImplementors().containsKey(MyIntf2.class));
		assertTrue( as.getImplementors().containsKey(MyIntf3.class));
		
		trace(as.toString());
		
				
	}

}
