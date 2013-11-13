/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

public interface IEnvelopeHandler {
    void process(IEnvelopeCtx ctx, Object data, IOutbox out);
}
