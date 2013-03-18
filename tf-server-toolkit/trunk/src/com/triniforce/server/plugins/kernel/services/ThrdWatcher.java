/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.services;

import java.text.MessageFormat;
import java.util.Map;

import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator.ThreadInfo;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class ThrdWatcher extends EPService{
	
	    @Override
	    public int getCyclePauseMs() {
	        return 1000 * 30;
	    }
	    
	    
	    @Override
	    public void doCycle() {
	        IThrdWatcherRegistrator twr = ApiStack
	                .getInterface(IThrdWatcherRegistrator.class);
	        
	        if (!twr.isAnyShortThreadWaiting(twr.getThreshold())){
	            return;
	        }
	        
	        Map<Thread, ThreadInfo> waitingThreads = twr.getWaitingThreads(twr.getThreshold());
	        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
	        String s="********************  Thread Watcher Info Begin\n";
	        for (Thread t : stacks.keySet()) {
	            if(!waitingThreads.containsKey(t)) continue;
	            s = s + MessageFormat.format("==================== Stack for {0} ({1}}\n", t.getName()
	                    ,waitingThreads.get(t).getThreadExtraInfo() ); 
	            
	            StackTraceElement[] trace = stacks.get(t);

	            for (StackTraceElement tr : trace) {
	                s = s + tr.toString() + "\n";
	            }
	        }
	        s += "********************  Thread Watcher Info End\n";        
	        ApiAlgs.getLog(this).error(s);
	        
	    }
}
