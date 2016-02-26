/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.triniforce.messagebus.error.EPublicationError;
import com.triniforce.messagebus.error.IPublicationErrorHandler;
import com.triniforce.utils.IMessageHandler;
import com.triniforce.utils.TFUtils;


public class TFMessageBus {
	
	LinkedHashMap<Class, Collection<IMessageHandler>> m_handlers = new LinkedHashMap<Class, Collection<IMessageHandler>>();
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

	public <T> void  subscribe(Class<T> msgClass, IMessageHandler<T> handler){

		WriteLock lock = m_rw.writeLock();
		lock.lock();
		try{
			Collection<IMessageHandler> classHandlers = m_handlers.get(msgClass);
			if (null == classHandlers) {
				classHandlers = new ArrayList<IMessageHandler>();
				m_handlers.put(msgClass, classHandlers);
			}
			classHandlers.add(handler);
		}finally{
			lock.unlock();
		}
	}
	
	public void  subscribeByAnnotation(Object listener){
		Class cls = listener.getClass();
		for(Method m: cls.getMethods()){
			if(null == m.getAnnotation(MessageHandler.class)){
				continue;
			}
			Class params[] = m.getParameterTypes();
			if (params.length != 1){
				continue;
			}
			subscribe(params[0], new MethodWrapper(listener, m));
		}
	}
	
	public <T> void  unsubscribe(Class<T> msgClass, IMessageHandler<T> handler){

		WriteLock lock = m_rw.writeLock();
		lock.lock();
		try{
			Collection<IMessageHandler> classHandlers = m_handlers.get(msgClass);
			if (null != classHandlers) {
				classHandlers.remove(handler);
			}
		}finally{
			lock.unlock();
		}
	}
	
	
	public Collection<IMessageHandler> getCopyOfClassHandlers(Class msgClass){
		Collection<IMessageHandler> res = new ArrayList<IMessageHandler>();
		ReadLock lock = m_rw.readLock();
		lock.lock();
		try{
			Collection<IMessageHandler> classHandlers = m_handlers.get(msgClass);
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
		
		Class curClass = message.getClass();
		while(true){
			Collection<IMessageHandler> handlers = getCopyOfClassHandlers(curClass);
			for(IMessageHandler handler: handlers){
				try{
					handler.onMessage(message);
				}catch(Exception cause){
					EPublicationError pe = new EPublicationError(cause, "Error handling message", handler, message);
					m_peh.handleError(pe);
				}
			}
			curClass = curClass.getSuperclass();
			if(null == curClass){
				break;
			}
		}
	}

}
