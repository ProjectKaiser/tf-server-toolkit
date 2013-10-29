/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.Collection;
import java.util.Map;

/**
 * Creates a street with given name on a given path with given boxes
 */
public class LTRAddStreet {
    private Collection<String> m_streetPath;
    private String m_streetName;
    private Map<String, IPOBox> m_boxes;

    public Collection<String> getStreetPath(){
        return m_streetPath;
    }

    public void setStreetPath(Collection<String> streetPath) {
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

    public void setBoxes(Map<String, IPOBox> boxes) {
        m_boxes = boxes;
    }

}
