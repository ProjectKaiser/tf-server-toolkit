/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.IPOBox;

public class POBoxWrapper {
    
    private final IPOBox m_box;

    public POBoxWrapper(IPOBox box) {
        m_box = box;

    }

    public IPOBox getBox() {
        return m_box;
    }

}
