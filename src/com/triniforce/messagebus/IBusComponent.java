/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

import java.util.List;

public interface IBusComponent {
    
    void handleMessage(BM msg, List<BMMsg> out);

}
