/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.pipe;

public interface IPipe extends IPushElement {
    /**
     * Adds push element to the tail of elements list
     */
    public void addPushElement(IPushElement element);
   
}
