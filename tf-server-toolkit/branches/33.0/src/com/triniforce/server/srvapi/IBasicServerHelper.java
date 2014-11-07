/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.srvapi;

import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.utils.ApiStack;

public class IBasicServerHelper {
    public static IPKExtensionPoint getExtensionPoint(Class cls){
        IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
        return bs.getExtensionPoint(cls);
    }

}
