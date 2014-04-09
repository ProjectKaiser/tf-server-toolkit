/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.pipe;

public interface IPushElement extends IPipeElement {
    /**
     * @return true if data must be passed to further elements, false otherwise
     */
    void push(Object data, IPipeElementFeedback fb);
}
