/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

import java.util.ArrayList;

public class StreetPath extends ArrayList<String>{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StreetPath() {
    }
    
    public StreetPath(String... names) {
        for (String name: names){
            add(name);
        }
    }
}
