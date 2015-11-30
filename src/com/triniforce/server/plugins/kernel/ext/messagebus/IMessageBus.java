/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.messagebus;

import com.triniforce.utils.IVoidMessageHandler;

public interface IMessageBus {
	public void publish(Object message);
	public <T> void  subscribe(Class<T> msgClass, IVoidMessageHandler<T> handler);
	public <T> void  unsubscribe(Class<T> msgClass, IVoidMessageHandler<T> handler);
}
