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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.triniforce.utils.IRunnable;


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

	private BusNamespace m_parent;
	
	protected void runWriteLocked(IRunnable run){
	    Lock lock = getRootLock().writeLock(); 
	    lock.lock();
	    try{
            try {
                run.run();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(),e);
            }
	    }finally{
	        lock.unlock();    
	    }
	}
	
	protected void setRootRecursively(BusNamespace root){
		//this comes as a root during disconect()
        if(null !=root && root != this){
        	m_rootLock = root.getRootLock();
            m_IEnqueueBM = root.getIEnqueueBM();
        }else{
        	m_rootLock = new ReentrantReadWriteLock();
            m_IEnqueueBM = null;
        	
        }
        for (BusNamespace ns : m_childs){
            ns.setRootRecursively(root);
        }
	}
	
    /**
     * 
     * Note: While method is working all access to this.childs from other
     * threads must be avoided somehow.
     * 
     * @param parent
     */
	public void connect(final BusNamespace parent){

        parent.getRootLock().writeLock().lock();
        try{
            m_parent = parent;
            setRootRecursively(parent);
            parent.m_childs.add(this);
        }finally{
            parent.getRootLock().writeLock().unlock();
        }
	}
	
	public void disconnect(){
		Lock lock = getRootLock().writeLock();
		lock.lock();
	    try{
	    	m_parent = null;
	        setRootRecursively(this);
	    }finally{
	        lock.unlock();
	    }
	}
	
    synchronized public void register(IBusComponent e){
    }
    
    synchronized public void register(IBusComponent e, String name){
    }
    
    synchronized void unregister(IBusComponent e){
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

	public BusNamespace getParent() {
		return m_parent;
	}

}
