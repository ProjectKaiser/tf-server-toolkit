/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBusNamespace {
    List<IBusElementStoppable> m_stoppable = new ArrayList<IBusElementStoppable>();
    List<EventBusNamespace> m_childs = new ArrayList<EventBusNamespace>();
    Map<BusAddressId, IBusElement> m_ids = new ConcurrentHashMap<BusAddressId, IBusElement>();
    Map<String, BusAddressId> m_urls = new ConcurrentHashMap<String, BusAddressId>();
	
    synchronized public BusAddressId register(IBusElement e){
        return null;
    }
    
    /**
     * 
     * Removes an element from a bus
     * @param e
     */
    synchronized void unregister(IBusElement e){
    }
    
    void giveURL(BusAddressId id, String URL){
    	m_urls.put(URL, id);   	
    }
    
    public BusAddressId queryByURL(String URL){
    	return m_urls.get(URL); 
    }
    
    public IBusElement queryById(BusAddressId id){
    	return m_ids.get(id);
    	
    }
    
    void subscribe(BusAddressId publisher, BusAddressId subscriber){
    	
    }
    
    
    /**
     * Calls all elements in all namespaces which implements {@link IBusElementStoppable}
     */
    void stop(){
    }
    
    void addNamespace(EventBusNamespace ns){
    	
    }
    void removeNamespace(EventBusNamespace ns){
    }

}
