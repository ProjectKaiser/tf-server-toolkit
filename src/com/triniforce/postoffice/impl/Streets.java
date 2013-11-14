/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.triniforce.postoffice.intf.StreetPath;

public class Streets extends ConcurrentHashMap<String, Street> {
    private static final long serialVersionUID = 1L;
    /**
     * @param path
     * @return null if not found
     */
    public Street queryPath(StreetPath path){
        Street res = null;
        Streets streets = this;
        for(String street: path){
            res = streets.get(street);
            if(null == res){
                return null;
            }
            streets = res.getStreets();
        }
        return res;
    }

}
