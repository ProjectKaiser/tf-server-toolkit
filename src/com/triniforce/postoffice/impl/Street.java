/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.triniforce.postoffice.intf.POBoxes;
import com.triniforce.postoffice.intf.StreetPath;

public class Street {
    private Streets m_streets = new Streets();
    private ConcurrentHashMap<String, POBoxWrapper> m_boxes = new ConcurrentHashMap();

    
    public Street() {
    }
    
    public Street(POBoxes boxes) {
        if(null == boxes){
            return;
        }
        for(String key: boxes.keySet()){
            m_boxes.put(key,  new POBoxWrapper(boxes.get(key)));
        }
    }
    
    public Streets getStreets() {
        return m_streets;
    }

    public void setChilds(Streets childs) {
        m_streets = childs;
    }
    
    /**
     * @param path null or empty means this
     * @return
     */
    public Street queryPath(StreetPath path){
        if( null == path){
            return this;
        }
        if(0 == path.size()){
            return this;
        }
        return m_streets.queryPath(path);
            
        
    }

    public ConcurrentHashMap<String, POBoxWrapper> getBoxes() {
        return m_boxes;
    }

}
