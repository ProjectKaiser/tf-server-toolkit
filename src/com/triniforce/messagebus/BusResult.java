/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

public class BusResult extends BusMsg{

	public BusResult(BusCall call, Object data) {
		super(data);
	}
	public BusResult(BusResult result, Object data) {
		super(data);
	}

}
