/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.UUID;

import com.triniforce.postoffice.intf.IPOBox;

public class POBoxWrapper {
    
    private UUID m_uuid;
    
    private final IPOBox m_box;

    public POBoxWrapper(IPOBox box) {
        m_box = box;

    }

    public IPOBox getBox() {
        return m_box;
    }

    public UUID getUuid() {
        return m_uuid;
    }

    public void setUuid(UUID uuid) {
        m_uuid = uuid;
    }

}
