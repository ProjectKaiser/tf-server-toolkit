/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

public class Street {
    private Streets m_childs = new Streets();;

    public Streets getChilds() {
        return m_childs;
    }

    public void setChilds(Streets childs) {
        m_childs = childs;
    }

}
