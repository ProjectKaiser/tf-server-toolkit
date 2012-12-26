/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.srvapi;

import com.triniforce.utils.IFinitable;
import com.triniforce.utils.IInitable;

public abstract class InitFinitTask  implements IInitable, IFinitable, Runnable{
    public void init() {
    }
    public void finit() {
    }    

}
