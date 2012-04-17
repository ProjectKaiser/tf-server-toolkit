/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.pipe;

import java.util.ArrayList;
import java.util.List;


/**
 * Collects all coming data into list. List is returned by getCollectedList()
 */
public class DataCollector implements IPushElement{

    public void push(Object data, IPipeElementFeedback fb) {
       m_list.add(data);
    }
    public List<Object> getCollectedList(){
        return m_list;
    }
    List<Object> m_list = new ArrayList<Object>();    

}
