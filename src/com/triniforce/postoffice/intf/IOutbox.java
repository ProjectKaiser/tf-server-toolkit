/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

public interface IOutbox {
    
    void post(String addr, Object data, IEnvelopeHandler replyHandler);
    void post(Class addr, Object data, IEnvelopeHandler replyHandler);
    void reply(Envelope envelope, Object data, IEnvelopeHandler replyHandler);

}
