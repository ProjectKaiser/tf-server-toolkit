/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

import java.util.HashMap;

/**
 * Puts POBox to a map using class name as a key
 */
public class POBoxes extends HashMap<String, IPOBox>{
    private static final long serialVersionUID = 1L;

    /**
     *  Puts POBox to a map using class name as a key
     */
    void put(IPOBox box){
        put(box.getClass().getName(), box);
    }
    
}
