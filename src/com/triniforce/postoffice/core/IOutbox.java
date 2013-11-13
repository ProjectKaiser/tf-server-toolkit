/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.core;

public interface IOutbox {
    
    void post(String addr, Object data);
    void post(Class addr, Object data);
    void post(Envelope envelope, Object data);
    
    void call(String addr, Object data, IEnvelopeHandler eh);
    void call(Class addr, Object data, IEnvelopeHandler eh);
    void call(Envelope envelope, Object data, IEnvelopeHandler eh);

}
