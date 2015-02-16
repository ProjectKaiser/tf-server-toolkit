/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

import com.triniforce.server.srvapi.BasicServerTask;

/**
 * User of IQSyncManager must provide some function explained in this interface
 * 
 */
public interface IQSyncManagerExternals {
	
	static class EQSyncerNotFound extends RuntimeException{
		private static final long serialVersionUID = 1L;
	
		public EQSyncerNotFound(String msg) {
			super(msg);
		}
	}
    
    /**
     * 
     * @param qid
     * @param syncerId
     * @return null if syncer is not registered
     */
    IQSyncer getQSyncer(long qid, Long syncerId) throws EQSyncerNotFound;

    /**
     * Execute runnable asynchronously. E.g. using {@link BasicServerTask}
     * @param r
     */
    void runSync(Runnable r);
}
