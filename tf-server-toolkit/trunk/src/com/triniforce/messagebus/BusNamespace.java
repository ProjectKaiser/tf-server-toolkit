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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class BusNamespace{
    
    public enum BusStatus{NOT_STARTED, STARTING, STARTED, STOPPING, STOPPED};
    
    private BusStatus m_status = BusStatus.NOT_STARTED;
    private ReentrantReadWriteLock m_rootLock = new ReentrantReadWriteLock();
    
    /**
     * Comes from parent 
     */
    protected IEnqueueBM m_IEnqueueBM;
	
    private List<BusNamespace> m_childs = new ArrayList<BusNamespace>();
    Map<String, BusComponent> m_urls = new ConcurrentHashMap<String, BusComponent>();
    ConcurrentLinkedQueue<BM> m_pendinq;

	BusNamespace m_parent;
	
	protected void connectRecursively(BusNamespace parent, boolean connect){
        m_rootLock = parent.getRootLock();
        m_IEnqueueBM = parent.getIEnqueueBM();
        for (BusNamespace ns : m_childs){
            ns.connectRecursively(this, connect);
        }
	}
	
    /**
     * 
     * Note: While method is working all access to this.childs from other
     * threads must be avoided somehow.
     * 
     * @param parent
     */
	public void connect(BusNamespace parent){
	    parent.getRootLock().writeLock().lock();
	    try{
	        connectRecursively(parent, true);
	        parent.m_childs.add(this);
	    }finally{
	        parent.getRootLock().writeLock().unlock();
	    }
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
    
    boolean tryHandleMessage(String name, BMMsg cmd, List<BMMsg> out){
    	return false;
    }


    public BusStatus getStatus() {
        return m_status;
    }


    public IEnqueueBM getIEnqueueBM() {
        return m_IEnqueueBM;
    }


    public ReentrantReadWriteLock getRootLock() {
        return m_rootLock;
    }


    public List<BusNamespace> getChilds() {
        return m_childs;
    }

}
