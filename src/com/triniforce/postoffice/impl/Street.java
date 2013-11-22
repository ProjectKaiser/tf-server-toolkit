/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.StreetPath;

public class Street {
    final private Street m_parent;
    final private NamedStreets m_streets = new NamedStreets();
    final private NamedPOBoxWrappers m_boxes = new NamedPOBoxWrappers();

    public Street() {
        this(null);
    }
    
    
    public Street(Street parent) {
        m_parent = parent;
        
    }
    
    public NamedStreets getStreets() {
        return m_streets;
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

    public NamedPOBoxWrappers getBoxes(){
        return m_boxes;
    }

    public Street getParent() {
        return m_parent;
    }

}
