/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.api;

public interface IPKEPAPIPeriodical extends Runnable{
    int getInitialDelayMs();
    int getDelayMs();
}
