/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

public interface IOutbox {
    
    void post(String addr, Object data);
    void post(Class addr, Object data);
    
    void post(Envelope envelope, Object data);

}
