/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.messagebus;

import net.engio.mbassy.listener.Handler;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.utils.ApiStack;

public class PKMessageBusTest extends BasicServerRunningTestCase {
	
	int cnt;
	
	@Handler
	void stringHandler(String msg){
		cnt++;
	}
	
	@Handler
	void stringHandler2(String msg){
		cnt++;
		throw new RuntimeException("stringHandler2");
	}
	
	@Override
	public void test() throws Exception {
		
		IMessageBus imb = ApiStack.getInterface(IMessageBus.class);
		PKMessageBus mb = (PKMessageBus) imb;
		mb.subscribe(this);
		
		cnt = 0;
		incExpectedLogErrorCount(1);
		imb.publish("Hello");
		
		assertEquals(2, cnt);
		
	}

}
