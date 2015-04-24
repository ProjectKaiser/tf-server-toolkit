/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.qsync.intf;

import java.io.PrintWriter;
import java.io.StringWriter;

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
    
    public QSyncTaskResult(long qid, long sid, QSyncTaskStatus status ,Exception e) {
		this.qid = qid;
		this.syncerId = sid;
		this.status = status;
		if(null != e){
			this.status = status;
			this.errorClass = e.getClass().getName();
			this.errorMessage = e.getMessage();
			StringWriter sw = new StringWriter();
			PrintWriter s = new PrintWriter(sw);
			e.printStackTrace(s);
			this.errorStack = sw.toString();
		}
	}

    public QSyncTaskResult(long qid, long sid, QSyncTaskStatus status , String cls, String msg, String stk) {
		this.qid = qid;
		this.syncerId = sid;
		this.status = status;
		this.status = status;
		this.errorClass = cls;
		this.errorMessage = msg;
		this.errorStack = stk;
	}
    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Status : " + status.toString() + "("+qid+")"+"\n");
    	if(null != errorClass)
    		sb.append("Class: " + errorClass + "\n" +
    			"Message: " + errorMessage + "\n" +
    			"stack: " + errorStack);
    	return sb.toString();
    }
    
}
