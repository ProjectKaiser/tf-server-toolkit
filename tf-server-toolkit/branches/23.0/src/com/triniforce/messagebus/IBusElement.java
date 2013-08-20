/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

import java.util.List;

public interface IBusElement {
    
    void handleMessage(BM msg, List<BMMsg> out);

}
