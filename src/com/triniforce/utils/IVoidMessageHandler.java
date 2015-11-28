/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.utils;

public interface IVoidMessageHandler<T>{
	public void onMessage(T arg) throws Exception;
}
