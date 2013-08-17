/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.messagebus;

public class BMResult extends BMMsg{

	public BMResult(BMCall call, Object data) {
		super(data);
	}
	public BMResult(BMResult result, Object data) {
		super(data);
	}

}
