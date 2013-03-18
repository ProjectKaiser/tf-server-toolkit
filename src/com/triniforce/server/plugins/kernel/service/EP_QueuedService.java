/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.service;

import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiStack;

/**
 * 
 * Service which process queue defined by its id.
 * <p>
 * Descendants must overrid doCycle() method
 * <p>
 * cycled pause = 0
 * queue wait = 5 minutes
 * 
 */
public class EP_QueuedService extends EPService {

    IDbQueue m_queue;

    ISrvSmartTranFactory m_tf;

    protected Object m_item;

	private long m_id;
    
    public EP_QueuedService(long queueId) {
		m_id = queueId;
	}

    @Override
    public int getCyclePauseMs() {
        return 0;
    }

    @Override
    public void init() {
        super.init();
        IDbQueueFactory qf = ApiStack.getApi().getIntfImplementor(
                IDbQueueFactory.class);
        m_tf = ApiStack.getApi().getIntfImplementor(ISrvSmartTranFactory.class);
        m_queue = qf.getDbQueue(m_id);
    }

    @Override
    public void initCycle() {       
        while (true) {
            m_tf.push();
            IThrdWatcherRegistrator itw = ApiStack.getInterface(IThrdWatcherRegistrator.class);
            itw.registerLongTermOp(Thread.currentThread());
            try{
                m_item = m_queue.get(1000*60*5);//5 minutes
            }finally{
                itw.unregisterLongTermOp(Thread.currentThread());    
            }
            if (m_item != null)
                break;
            m_tf.pop();
        }
    }

    @Override
    public void commitCycle() {
        ISrvSmartTran t = ApiStack.getApi().getIntfImplementor(
                ISrvSmartTran.class);
        t.commit();
        super.commitCycle();
    }

    @Override
    public void finitCycle() {
        m_item = null;
        m_tf.pop();
        super.finitCycle();
    }
    
    public long getId(){
    	return m_id;
    }
}
