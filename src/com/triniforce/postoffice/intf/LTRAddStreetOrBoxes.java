/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;


/**
 * 
 * If m_streetName is not null, creates a street with given name on a given path with given boxes.
 * If m_streeName is null, adds boxes to given street.
 * 
 */
public class LTRAddStreetOrBoxes {
    
    private StreetPath m_streetPath;
    private String m_newStreetName;
    private NamedPOBoxes m_boxes;

    public LTRAddStreetOrBoxes() {

    }
    
    /**
     * @param streetPath null means root
     * @param streetName
     * @param boxes null means no boxes
     */
    public LTRAddStreetOrBoxes(StreetPath streetPath, String streetName, NamedPOBoxes boxes) {
        m_streetPath = streetPath;
        m_newStreetName = streetName;
        m_boxes = boxes;

    }
    
    public StreetPath getStreetPath(){
        return m_streetPath;
    }

    public void setStreetPath(StreetPath streetPath) {
        m_streetPath = streetPath;
    }

    public String getNewStreetName() {
        return m_newStreetName;
    }

    public void setNewStreetName(String streetName) {
        m_newStreetName = streetName;
    }

    public NamedPOBoxes getBoxes() {
        return m_boxes;
    }

    public void setBoxes(NamedPOBoxes boxes) {
        m_boxes = boxes;
    }

}
