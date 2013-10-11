/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

public interface IOutbox {
    
    void post(String addr, Object data);
    void post(Class addr, Object data);
    
    /**
     * @param addr
     *            If Class addr.getName() will be used.
     *            <p>Otherwise addr.toString() is used
     * @param data
     * @param callBack
     *            if null default caller interface will be used
     */
    void post(Object addr, Object data, IRecipient callBack);

    /**
     * 
     * Same as {@link IOutbox#post(Object, Object, IRecipient)} but sender's envelope is
     * used to locate recipient
     * 
     */
    void post(IEnvelope sender, Object data, IRecipient callBack);

}
