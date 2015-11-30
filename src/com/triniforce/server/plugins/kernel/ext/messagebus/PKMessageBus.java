/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.messagebus;

import com.triniforce.messagebus.TFMessageBus;
import com.triniforce.messagebus.error.EPublicationError;
import com.triniforce.messagebus.error.IPublicationErrorHandler;
import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IMessageHandler;

public class PKMessageBus implements IPKEPAPI, IMessageBus{

	TFMessageBus m_bus;
	
	public PKMessageBus(){
		IPublicationErrorHandler peh = new IPublicationErrorHandler(){

			@Override
			public void handleError(EPublicationError error) {
				ApiAlgs.getLog(PKMessageBus.class).error(error);
			}
			
		};		
		m_bus = new TFMessageBus(peh);
	}
	
	@Override
	public void publish(Object message) {
		m_bus.publish(message);
		
	}

	@Override
	public Class getImplementedInterface() {
		return IMessageBus.class;
	}

	@Override
	public <T> void subscribe(Class<T> msgClass, IMessageHandler<T> handler) {
		m_bus.subscribe(msgClass, handler);
		
	}

	@Override
	public <T> void unsubscribe(Class<T> msgClass, IMessageHandler<T> handler) {
		m_bus.unsubscribe(msgClass, handler);
	}
	
}
