/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

public interface IPOBox extends IEnvelopeHandler{
    void shutdown(int pauseMs);
    void connect();
    void disconnect();
}
