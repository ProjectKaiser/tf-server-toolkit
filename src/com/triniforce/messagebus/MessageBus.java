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

import com.triniforce.utils.InSeparateThreadExecutor.IRunnable;

/**
 *
 */
public class MessageBus implements IMessageBus{
    
    public enum BusStatus{NOT_STARTED, STARTING, STARTED, STOPPING, STOPPED};
    
    private BusStatus m_status = BusStatus.NOT_STARTED;
    private ReentrantReadWriteLock m_rootLock = new ReentrantReadWriteLock();
    
    /**
     * Comes from parent 
     */
    protected IPostMaster m_postMaster;
	
    private List<MessageBus> m_childs = new ArrayList<MessageBus>();
    Map<String, BusComponent> m_urls = new ConcurrentHashMap<String, BusComponent>();

	private MessageBus m_parent;
	
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
	
	protected void setRootRecursively(MessageBus root){
		//this comes as a root during disconect()
        if(null !=root && root != this){
        	m_rootLock = root.getRootLock();
            m_postMaster = root.getPostMaster();
        }else{
        	m_rootLock = new ReentrantReadWriteLock();
            m_postMaster = null;
        	
        }
        for (MessageBus ns : m_childs){
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
	public void connectToParentBus(final MessageBus parent){

        parent.getRootLock().writeLock().lock();
        try{
            m_parent = parent;
            setRootRecursively(parent);
            parent.m_childs.add(this);
        }finally{
            parent.getRootLock().writeLock().unlock();
        }
	}
	
	public void disconnectFromParentBus(){
		Lock lock = getRootLock().writeLock();
		lock.lock();
	    try{
	    	m_parent = null;
	        setRootRecursively(this);
	    }finally{
	        lock.unlock();
	    }
	}
	
    synchronized public void registerComponent(IBusComponent e){
    }
    
    synchronized public void registerComponent(IBusComponent e, String name){
    }
    
    synchronized public void unregisterComponent(IBusComponent e){
    }
    
    public void subscribe(String publisher, String subscriber){
    }
    public void subscribe(Class publisher, Class subscriber){
    }
    
    boolean tryHandleMessage(String name, BMMsg cmd, List<BMMsg> out){
    	return false;
    }


    public BusStatus getStatus() {
        return m_status;
    }


    public IPostMaster getPostMaster() {
        return m_postMaster;
    }


    public ReentrantReadWriteLock getRootLock() {
        return m_rootLock;
    }


    public List<MessageBus> getChilds() {
        return m_childs;
    }

	public MessageBus getParent() {
		return m_parent;
	}

    public void deliverMessage(MessageBus srcBus, BusComponent srcComponent,
            BM bm) {
        
    }

}
