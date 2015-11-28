/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.triniforce.messagebus.error.EPublicationError;
import com.triniforce.messagebus.error.IPublicationErrorHandler;
import com.triniforce.utils.IVoidMessageHandler;
import com.triniforce.utils.TFUtils;

public class TFMessageBus {
	
	LinkedHashMap<Class, Collection<IVoidMessageHandler>> m_handlers = new LinkedHashMap<Class, Collection<IVoidMessageHandler>>();
	ReentrantReadWriteLock m_rw = new ReentrantReadWriteLock();
	private final IPublicationErrorHandler m_peh;
	
	public TFMessageBus() {
		this(new IPublicationErrorHandler() {
			@Override
			public void handleError(EPublicationError error) {
			}
		});
	}
	
	public TFMessageBus(IPublicationErrorHandler peh) {
		TFUtils.assertNotNull(peh, "peh");
		m_peh = peh;
	}
	
	public <T> void  subscribe(Class<T> msgClass, IVoidMessageHandler<T> handler){

		WriteLock lock = m_rw.writeLock();
		lock.lock();
		try{
			Collection<IVoidMessageHandler> classHandlers = m_handlers.get(msgClass);
			if (null == classHandlers) {
				classHandlers = new ArrayList<IVoidMessageHandler>();
				m_handlers.put(msgClass, classHandlers);
			}
			classHandlers.add(handler);
		}finally{
			lock.unlock();
		}
	}
	
	public <T> void  unsubscribe(Class<T> msgClass, IVoidMessageHandler<T> handler){

		WriteLock lock = m_rw.writeLock();
		lock.lock();
		try{
			Collection<IVoidMessageHandler> classHandlers = m_handlers.get(msgClass);
			if (null != classHandlers) {
				classHandlers.remove(handler);
			}
		}finally{
			lock.unlock();
		}
	}
	
	
	public Collection<IVoidMessageHandler> getCopyOfClassHandlers(Class msgClass){
		Collection<IVoidMessageHandler> res = new ArrayList<IVoidMessageHandler>();
		ReadLock lock = m_rw.readLock();
		lock.lock();
		try{
			Collection<IVoidMessageHandler> classHandlers = m_handlers.get(msgClass);
			if (null == classHandlers) {
				return res;
			}
			res.addAll(classHandlers);
			return res;
		}finally{
			lock.unlock();
		}
	}
	
	public void publish(Object message){
		if(null == message){
			return;
		}
		Collection<IVoidMessageHandler> handlers = getCopyOfClassHandlers(message.getClass());
		for(IVoidMessageHandler handler: handlers){
			try{
				handler.onMessage(message);
			}catch(Exception cause){
				EPublicationError pe = new EPublicationError(cause, "Error handling message", handler, message);
				m_peh.handleError(pe);
			}
		}
	}

}
