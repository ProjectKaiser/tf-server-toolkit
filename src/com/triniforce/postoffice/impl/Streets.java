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
    public Street queryByPath(StreetPath streetPath){
        Street res = null;
        Streets streets = this;
        for(String street: streetPath){
            res = streets.get(street);
            if(null == res){
                return null;
            }
            streets = res.getChilds();
        }
        return res;
    }

}
