/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eventbus;

public interface IBusElementStoppable extends IBusElement{
    /**
     * Should finish all background activity and return
     */
    void stop();

}
