/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.syncbus.intf;

import java.text.MessageFormat;

@SuppressWarnings("serial")
public class ENoSuchPublisher extends RuntimeException{
	
	public ENoSuchPublisher(Object addr) {
		super(MessageFormat.format("Publisher with address {0} does not exist", addr));
	}

}
