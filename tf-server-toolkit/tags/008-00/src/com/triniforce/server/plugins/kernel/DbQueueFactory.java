/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 

package com.triniforce.server.plugins.kernel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.IIdGenerator;

public class DbQueueFactory implements IDbQueueFactory {
	private Map<Long, IDbQueue> m_queues;
	private IIdGenerator m_idGen;

	public DbQueueFactory(IIdGenerator idGen) {
		m_queues = Collections.synchronizedMap(new HashMap<Long, IDbQueue>());
		m_idGen = idGen;
	}
	
	public IDbQueue getDbQueue(long queueId) {

		IDbQueue res = m_queues.get(queueId);
		if(null == res){
			res = new DbQueue(queueId, m_idGen);
			m_queues.put(queueId, res);
		}
		return res;
	}

}
