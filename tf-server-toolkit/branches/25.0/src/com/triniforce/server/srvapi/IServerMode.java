/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.srvapi;

public interface IServerMode {
    IBasicServer.Mode getMode();
    int getStackSize();
}
