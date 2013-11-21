/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;


/**
 * Creates a street with given name on a given path with given boxes
 */
public class LTRAddStreet {
    private StreetPath m_streetPath;
    private String m_streetName;
    private NamedPOBoxes m_boxes;

    public LTRAddStreet() {

    }
    
    /**
     * @param streetPath null means root
     * @param streetName
     * @param boxes null means no boxes
     */
    public LTRAddStreet(StreetPath streetPath, String streetName, NamedPOBoxes boxes) {
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

    public NamedPOBoxes getBoxes() {
        return m_boxes;
    }

    public void setBoxes(NamedPOBoxes boxes) {
        m_boxes = boxes;
    }

}
