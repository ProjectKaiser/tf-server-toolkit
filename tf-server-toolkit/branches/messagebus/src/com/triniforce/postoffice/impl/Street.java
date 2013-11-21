/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.triniforce.postoffice.intf.NamedPOBoxes;
import com.triniforce.postoffice.intf.StreetPath;

public class Street {
    private Street m_parent;
    private NamedStreets m_streets = new NamedStreets();
    private NamedPOBoxWrappers m_boxes = new NamedPOBoxWrappers();

    
    public Street(){
    }
    
    public Street(NamedPOBoxes boxes) {
        if(null == boxes){
            return;
        }
        for(String key: boxes.keySet()){
            m_boxes.put(key,  new POBoxWrapper(boxes.get(key)));
        }
    }
    
    public NamedStreets getStreets() {
        return m_streets;
    }

    public void setChilds(NamedStreets childs) {
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

    public Street getParent() {
        return m_parent;
    }

    public void setParent(Street parent) {
        m_parent = parent;
    }

}
