/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

import java.util.Collection;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.messagebus.error.EPublicationError;
import com.triniforce.messagebus.error.IPublicationErrorHandler;
import com.triniforce.utils.IMessageHandler;

public class TFMessageBusTest extends TFTestCase {
	
	@Override
	public void test() throws Exception {
		TFMessageBus mb = new TFMessageBus();
		
		IMessageHandler mh1 = new IMessageHandler<String>() {
			@Override
			public void onMessage(String arg) {
				trace(arg);
			}
		};
		
		IMessageHandler<String> mh2 = new IMessageHandler<String>() {
			@Override
			public void onMessage(String arg) {
				trace(arg);
			}
		};
		
		assertEquals(0, mb.getCopyOfClassHandlers(String.class).size());
		
		mb.subscribe(String.class, mh1);
		assertEquals(1, mb.getCopyOfClassHandlers(String.class).size());
		
		//subsribe mh2 second time
		
		mb.subscribe(String.class, mh1);
		assertEquals(2, mb.getCopyOfClassHandlers(String.class).size());
		
		mb.subscribe(String.class, mh2);
		Collection<IMessageHandler> handlers = mb.getCopyOfClassHandlers(String.class);
		assertEquals(3, handlers.size());
		handlers.remove(mh1);
		assertEquals(2, handlers.size());
		handlers.remove(mh1);
		assertEquals(1, handlers.size());
		
		//but original stay intact
		
		handlers = mb.getCopyOfClassHandlers(String.class);
		assertEquals(3, handlers.size());
		
		//unsubscribe
		
		mb.unsubscribe(String.class, mh1);
		handlers = mb.getCopyOfClassHandlers(String.class);
		assertEquals(2, handlers.size());
		
		//unsubscribe second time
		
		mb.unsubscribe(String.class, mh1);
		handlers = mb.getCopyOfClassHandlers(String.class);
		assertEquals(1, handlers.size());
		
		//unsubscribe third time :)
		
		mb.unsubscribe(String.class, mh1);
		handlers = mb.getCopyOfClassHandlers(String.class);
		assertEquals(1, handlers.size());
		
		
		
	
	}

	int m_cnt = 0;
	
	public void testPublish() throws Exception {
		
		IMessageHandler mh1 = new IMessageHandler<String>() {
			@Override
			public void onMessage(String arg) {
				m_cnt++;
				trace(arg);
			}
		};
		
		
		IMessageHandler mh2 = new IMessageHandler<String>() {
			@Override
			public void onMessage(String arg) {
				m_cnt++;
				throw new RuntimeException("My Problem");
			}
		};
		
		IMessageHandler mh3 = new IMessageHandler<String>() {
			@Override
			public void onMessage(String arg) {
				m_cnt++;
				trace(arg);
			}
		};

		
		TFMessageBus mb = new TFMessageBus();
		
		mb.subscribe(String.class, mh1);
		mb.subscribe(String.class, mh2);
		mb.subscribe(String.class, mh3);
		
		m_cnt = 0;
		mb.publish("Hello");
		assertEquals(3, m_cnt);
	}
	
	public static class MyClass{};
	public static class MyClass2 extends MyClass{};
	
	public void testPublishSuperclass() throws Exception {
		
		IMessageHandler mh1 = new IMessageHandler<MyClass>() {
			@Override
			public void onMessage(MyClass arg) {
				m_cnt++;
				trace(arg);
			}
		};
		
		TFMessageBus mb = new TFMessageBus();
		
		mb.subscribe(MyClass.class, mh1);
		
		m_cnt = 0;
		mb.publish(new MyClass2());
		assertEquals(1, m_cnt);
		
		
	}
	
	@MessageHandler
	public void handler1(String arg){
		trace("Hello");
		m_cnt++;
	}
	
	@MessageHandler
	public void handler2(Integer arg){
		throw new RuntimeException("handler2 error");
	}
	
	public void test_subscribeByAnnotation(){
		
		IPublicationErrorHandler peh = new IPublicationErrorHandler() {
			
			@Override
			public void handleError(EPublicationError error) {
				trace(error);
			}
		};
		
		TFMessageBus mb = new TFMessageBus(peh);
		mb.subscribeByAnnotation(this);
		
		m_cnt = 0;
		mb.publish("Hello!");
		assertEquals(1, m_cnt);
		
		//testexception
		mb.publish(12);		
		
	}

}
