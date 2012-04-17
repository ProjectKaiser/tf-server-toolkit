/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.srv_ev;

import com.triniforce.server.srvapi.IBasicServer;

public abstract class ServerEventHandler {
    public abstract void handleEvent(IBasicServer srv, ServerEvent event);    
}
