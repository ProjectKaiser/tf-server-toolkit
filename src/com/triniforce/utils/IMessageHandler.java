/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.utils;

public interface IMessageHandler<T>{
	public void onMessage(T arg) throws Exception;
}
