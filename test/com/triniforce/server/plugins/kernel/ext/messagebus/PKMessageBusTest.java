/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.messagebus;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IMessageHandler;

public class PKMessageBusTest extends BasicServerRunningTestCase {
	
	int cnt;
	
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
		
		
	}

}
