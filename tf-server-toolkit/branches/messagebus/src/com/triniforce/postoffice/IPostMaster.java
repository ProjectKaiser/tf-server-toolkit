/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.Map;

public interface IPostMaster {
    
    void createStreet(String parentStreet,  String street, Map<String, IPOBox> boxes);
    
    void connectPOBox(String street, IPOBox box);
    /**
     * @param street
     * @param box
     * @param pauseMs Just an info which is passed to POBox
     */
    
    void shutdownPOBox(String street, String box, int pauseMs);
    /**
     * 
     * If not called yet Calls shutdownPOBox with zero pause,
     * 
     * */
    
    void disconnectPOBox(String street, String boxKey);
    
    void shutdownStreet(String street, int pauseMs);
    void disconnectStreet(String street);
    
    
}
