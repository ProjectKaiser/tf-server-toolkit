/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

public interface IPOBox {

    void process(IEnvelope env, Object data, IOutbox out);
    void processShutdown(IEnvelope env, Object data, IOutbox out);

}
