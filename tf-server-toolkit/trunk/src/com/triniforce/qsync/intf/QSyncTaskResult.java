/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.qsync.intf;

public class QSyncTaskResult {

    public long qid;
    public long syncerId;

    public QSyncTaskStatus status;
    
    /**
     * Makes sense only if status is ERROR
     */;
    public String errorMessage;
    public String errorClass;
    public String errorStack;

    
    
}
