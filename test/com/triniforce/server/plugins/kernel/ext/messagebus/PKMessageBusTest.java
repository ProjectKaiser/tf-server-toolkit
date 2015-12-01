/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.messagebus;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.messagebus.MessageHandler;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IMessageHandler;

public class PKMessageBusTest extends BasicServerRunningTestCase {
	
	int cnt;
	
	@MessageHandler
	public String myHandler(Integer i){
		cnt++;
		return null;
	}
	
	@Override
	public void test() throws Exception {
		
		IMessageBus imb = ApiStack.getInterface(IMessageBus.class);

		IMessageHandler mh1 = new IMessageHandler<String>() {
			@Override
			public void onMessage(String arg) {
				cnt++;
				trace(arg);
			}
		};

		cnt = 0;
		imb.subscribe(String.class, mh1);
		imb.subscribe(String.class, mh1);
		imb.publish("Hello");
		assertEquals(2, cnt);
		
		// Unsibscribe first
		
		imb.unsubscribe(String.class, mh1);
		imb.publish("Hello");
		assertEquals(3, cnt);
		
		// Unsibscribe second
		
		imb.unsubscribe(String.class, mh1);
		imb.publish("Hello");
		assertEquals(3, cnt);
		
		cnt = 0;

		imb.publish(12);
		assertEquals(0, cnt);
		
		imb.subscribeByAnnotation(this);
		imb.publish(12);
		assertEquals(1, cnt);
		
	}
}
