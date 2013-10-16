/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.Map;

public interface IPostMaster {
    String createNamespace();
    String createNamespace(Map<String, IPOBox> boxes);
    
    void connectPOBox(String nsKey, IPOBox box);
    /**
     * @param nsKey
     * @param boxKey
     * @param pauseMs Just an info which is passed to POBox
     */
    void shutdownPOBox(String nsKey, String boxKey, int pauseMs);
    /**
     * 
     * If not called yet Calls shutdownPOBox with zero pause, .
     * 
     * */
    void disconnectPOBox(String nsKey, String boxKey);
    
    void shutdownNamespace(String nsKey, int pauseMs);
    void disconnectNamespace(String nsKey);
    
    
}
