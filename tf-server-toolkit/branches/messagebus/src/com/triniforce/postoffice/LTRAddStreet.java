/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.Map;

/**
 * Creates a street with given name on a given path with given boxes
 */
public class LTRAddStreet {
    private StreetPath m_streetPath;
    private String m_streetName;
    private POBoxes m_boxes;

    public LTRAddStreet() {

    }
    
    public LTRAddStreet(StreetPath streetPath, String streetName, POBoxes boxes) {
        m_streetPath = streetPath;
        m_streetName = streetName;
        m_boxes = boxes;

    }
    
    public StreetPath getStreetPath(){
        return m_streetPath;
    }

    public void setStreetPath(StreetPath streetPath) {
        m_streetPath = streetPath;
    }

    public String getStreetName() {
        return m_streetName;
    }

    public void setStreetName(String streetName) {
        m_streetName = streetName;
    }

    public Map<String, IPOBox> getBoxes() {
        return m_boxes;
    }

    public void setBoxes(POBoxes boxes) {
        m_boxes = boxes;
    }

}
