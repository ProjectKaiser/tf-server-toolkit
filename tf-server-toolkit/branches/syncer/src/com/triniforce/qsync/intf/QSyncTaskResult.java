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
     * Makes sense only if status is ERROR or NOT_STARTED
     */;
    public String errorMessage;
    public String errorClass;
    public String errorStack;

    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Status : " + status.toString() + "\n" +
    			"Class: " + errorClass + "\n" +
    			"Message: " + errorMessage + "\n" +
    			"stack: " + errorStack);
    	return sb.toString();
    }
    
}
