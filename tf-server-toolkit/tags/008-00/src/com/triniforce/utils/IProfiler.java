/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

public interface IProfiler {

    void reportItem(String group, String itemName, long total, long childs, boolean bInner );
    String toString();
	void clearResult();
	
	public class ActivityStatus {
		 IProfilerStack ps;
		 Object idleInfo;
		 
		 public ActivityStatus(IProfilerStack ps, Object idleInfo) {
			 this.ps = ps;
			 this.idleInfo = idleInfo;
		 }
	}
	void updateActivityStatus( Object activityKey, ActivityStatus status);
	void removeActivityStatus(Object activityKey);
	String getSnapshot();
    
}
