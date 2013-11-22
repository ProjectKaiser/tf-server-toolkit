/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

import java.util.HashMap;

/**
 * Purpose is to put a POBox to a map using class name as a key
 */
public class NamedPOBoxes extends HashMap<String, IPOBox>{
    private static final long serialVersionUID = 1L;

    /**
     *  Puts POBox to a map using class name as a key
     */
    public void putByClass(IPOBox box){
        put(box.getClass().getName(), box);
    }
    
}
