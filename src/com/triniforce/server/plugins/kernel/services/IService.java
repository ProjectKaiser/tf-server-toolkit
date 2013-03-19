/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.services;



public interface IService{  

    public enum State {
        STOPPED, STARTING, RUNNING, STOPPING
    };   
    
    /**
     * This method returns when state comes to any of [RUNNING, STOPPED] 
     */
    void start();
    
    /**
     * This method returns when state comes to any of [STOPPED] 
     */
    void stop();
    
    /**
     * @return
     */
    State getState();
    
}
