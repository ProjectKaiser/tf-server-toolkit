/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BusNamespace{
	
	static class ElementWrapper{
		protected final IBusElement m_e;
		public ElementWrapper(IBusElement e){
			m_e = e;
		}
	}
	
    List<IBusElementStoppable> m_stoppable = new ArrayList<IBusElementStoppable>();
    List<BusNamespace> m_childs = new ArrayList<BusNamespace>();
    Map<String, ElementWrapper> m_urls = new ConcurrentHashMap<String, ElementWrapper>();

	BusNamespace m_parent;
	
	void setParent(BusNamespace ns){
		m_parent = ns;
	}
	
	
    synchronized public void register(IBusElement e){
    }
    
    synchronized public void register(IBusElement e, String name){
    }
    
    synchronized void unregister(IBusElement e){
    }
    
    public void subscribe(String publisher, String subscriber){
    }
    public void subscribe(Class publisher, Class subscriber){
    }
    
    /**
     * Calls all elements in all namespaces which implements {@link IBusElementStoppable}
     */
    void stop(){
    }
    
    void addNamespace(BusNamespace ns){
    	
    }
    void removeNamespace(BusNamespace ns){
    }
    
    
    boolean tryHandleMessage(String name, BusMsg cmd, List<BusMsg> out){
    	return false;
    }

}
