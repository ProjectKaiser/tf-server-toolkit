/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.syncbus.intf;

import java.text.MessageFormat;

@SuppressWarnings("serial")
public class EInvalidSubscriberType extends RuntimeException{
	public EInvalidSubscriberType(Object addr, ISubscriber expected, ISubscriber real){
		super(MessageFormat.format("Publisher with address {0} expects subscriber type {1} but {2} used", addr, expected, real));
	}

}
