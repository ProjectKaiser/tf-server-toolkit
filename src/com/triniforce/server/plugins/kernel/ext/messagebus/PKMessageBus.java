/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.messagebus;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.utils.ApiAlgs;

public class PKMessageBus implements IPKEPAPI, IMessageBus{

	MBassador m_bus;
	
	public PKMessageBus(){
		IPublicationErrorHandler peh = new IPublicationErrorHandler(){

			@Override
			public void handleError(PublicationError error) {
				ApiAlgs.getLog(PKMessageBus.class).error(error);
			}
			
		};		
		m_bus = new MBassador(peh);
	}
	
	@Override
	public void publish(Object message) {
		m_bus.publish(message);
		
	}

	@Override
	public Class getImplementedInterface() {
		return IMessageBus.class;
	}
	
	public void subscribe(Object o){
		m_bus.subscribe(o);
	}
	
	public void unsubscribe(Object o){
		m_bus.unsubscribe(o);
	}

}
