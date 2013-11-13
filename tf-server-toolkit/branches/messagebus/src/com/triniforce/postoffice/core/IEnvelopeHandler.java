/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.core;

public interface IEnvelopeHandler {
    void process(IEnvelopeCtx ctx, Object data, IOutbox out);
}
