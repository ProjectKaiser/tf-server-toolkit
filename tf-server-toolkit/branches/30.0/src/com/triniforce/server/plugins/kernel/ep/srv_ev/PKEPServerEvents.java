/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.srv_ev;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.srvapi.IBasicServer;

public class PKEPServerEvents extends PKExtensionPoint{
    public PKEPServerEvents() {
        setSingleExtensionInstances(true);
        setExtensionClass(ServerEventHandler.class);
    }
    
    public void handleEventDirectOrder(IBasicServer srv, ServerEvent event){
        handleEvent(srv, event, false);
    }
    public void handleEventReverseOrder(IBasicServer srv, ServerEvent event){
        handleEvent(srv, event, true);
    }    
    
    protected void handleEvent(IBasicServer srv, ServerEvent event, boolean revertOrder){
        List<String> ids = new ArrayList<String>(getExtensions().size());
        for(String id:getExtensions().keySet()){
            ids.add(id);
        }
        if(revertOrder){
            Collections.reverse(ids);
        }
        for(String id: ids){
        	event.init(srv);
        	try{
        		ServerEventHandler h = getExtension(id).getInstance();
        		h.handleEvent(srv, event);
        	}finally{
        		event.finit(srv);
        	}
        }
    }

}
