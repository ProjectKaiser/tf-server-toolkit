/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eventbus;

import java.util.ArrayList;
import java.util.List;

public class EventBus{
    List<IBusElementStoppable> m_stoppable = new ArrayList<IBusElementStoppable>();
    
    synchronized BusAddressId add(IBusElement e){
        return null;
    }
    
    /**
     * Calls all elements which implement {@link IBusElementStoppable}
     */
    void stop(){
    }
    
}
