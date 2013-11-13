/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.triniforce.postoffice.intf.IPostMaster;
import com.triniforce.postoffice.intf.LTRGetStreets;
import com.triniforce.postoffice.intf.StreetPath;

public class PostMaster implements IPostMaster{
    
    Streets m_rootStreets = new Streets();
    ExecutorService m_es;
    
    
    public PostMaster() {
        m_es = Executors.newFixedThreadPool(20);
    }
    public PostMaster(ExecutorService es) {
        m_es = es;
    }
    
    public Future post(StreetPath streetPath, String box, Object data){
        PostTask ft = new PostTask(this, null, streetPath, box, data, null);
        return m_es.submit(ft);
    }

    Object process(Object data){
        Object res = null;
        if( data instanceof LTRGetStreets){
            res = new ArrayList<String>(m_rootStreets.keySet());
        }
        return res;
    }
    public void stop(int waitMilliseconds) {
        m_es.shutdown();
        try{
            m_es.awaitTermination(waitMilliseconds, TimeUnit.MILLISECONDS);
        }catch(Exception e){
        }
    }

}
