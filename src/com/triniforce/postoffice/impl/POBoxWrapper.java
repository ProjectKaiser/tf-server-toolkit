/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.UUID;

import com.triniforce.postoffice.intf.IPOBox;

public class POBoxWrapper {
    
    final private UUID m_uuid;
    
    final private IPOBox m_box;
    
    final private Street m_parent;

    public POBoxWrapper(Street parent, IPOBox box, UUID uuid) {
        m_box = box;
        m_parent = parent;
        m_uuid = uuid;

    }

    public IPOBox getBox() {
        return m_box;
    }

    public UUID getUuid() {
        return m_uuid;
    }

    public Street getParent() {
        return m_parent;
    }

}
