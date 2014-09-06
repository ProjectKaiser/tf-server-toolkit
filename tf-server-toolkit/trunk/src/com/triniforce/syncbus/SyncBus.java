/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import java.util.concurrent.ConcurrentHashMap;

import com.triniforce.syncbus.intf.ICOffer;
import com.triniforce.syncbus.intf.ICPublisher;
import com.triniforce.syncbus.intf.IIncOffer;
import com.triniforce.syncbus.intf.IIncPublisher;
import com.triniforce.syncbus.intf.IPublisher;
import com.triniforce.syncbus.intf.ISyncBus;


public class SyncBus implements ISyncBus{
	
	ConcurrentHashMap<Object, PubInstance> m_pubs = new ConcurrentHashMap<Object, PubInstance>(); 

	public ICOffer registerCPublisher(Object addr, ICPublisher cPub) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICOffer registerCPublisher(ICPublisher cPub) {
		// TODO Auto-generated method stub
		return null;
	}

	public IIncOffer registerIncPublisher(Object addr, IIncPublisher cPub) {
		// TODO Auto-generated method stub
		return null;
	}

	public IIncOffer registerIncPublisher(IIncPublisher cPub) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPublisher queryPub(Object addr) {
		// TODO Auto-generated method stub
		return null;
	}
}
