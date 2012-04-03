/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.recurring;

public abstract class PKEPRecurringTask {
    public abstract void processTask(long id, long start, long currentTime, boolean isTooLate);
    
}
