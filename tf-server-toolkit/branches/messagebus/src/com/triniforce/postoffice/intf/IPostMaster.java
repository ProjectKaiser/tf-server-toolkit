/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

import java.util.concurrent.Future;


public interface IPostMaster {
    
    public static final String ROOT_STREET = "root"; 
    
    Future post(StreetPath streetPath, String box, Object data);
    
    <T> T call(StreetPath streetPath, String box, Object data);
    
    void stop(int waitMilliseconds);
}
