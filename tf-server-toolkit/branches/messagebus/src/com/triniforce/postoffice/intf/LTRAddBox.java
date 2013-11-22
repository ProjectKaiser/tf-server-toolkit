/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

/**
 * Adds a box to a given street
 */
public class LTRAddBox {
    
    private StreetPath m_streetPath;
    private IPOBox m_box;
    private String m_boxName;
    
    public LTRAddBox(StreetPath streetPath, IPOBox box, String boxName){
        super();
        setStreetPath(streetPath);
        setBox(box);
        setBoxName(boxName);
    }

    public LTRAddBox() {
    }

    public StreetPath getStreetPath() {
        return m_streetPath;
    }

    public void setStreetPath(StreetPath streetPath) {
        m_streetPath = streetPath;
    }

    public IPOBox getBox() {
        return m_box;
    }

    public void setBox(IPOBox box) {
        m_box = box;
    }

    public String getBoxName() {
        return m_boxName;
    }

    public void setBoxName(String boxName) {
        m_boxName = boxName;
    }

    
}
