/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.HashMap;

public class POBoxes extends HashMap<String, IPOBox>{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    void put(IPOBox box){
        put(box.getClass().getName(), box);
    }
}
