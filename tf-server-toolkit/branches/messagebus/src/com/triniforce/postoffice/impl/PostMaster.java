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
import com.triniforce.postoffice.intf.LTRAddStreet;
import com.triniforce.postoffice.intf.LTRGetStreets;
import com.triniforce.postoffice.intf.StreetPath;

public class PostMaster implements IPostMaster{
    
    Streets m_rootStreets = new Streets();
    Street m_rootStreet = new Street();
    ExecutorService m_es;
    
    
    public PostMaster() {
        this(Executors.newFixedThreadPool(20));
    }
    public PostMaster(ExecutorService es) {
        m_es = es;
        m_rootStreets.put(IPostMaster.ROOT_STREET, m_rootStreet);
    }
    
    public Future post(StreetPath streetPath, String box, Object data){
        PostTask ft = new PostTask(this, null, streetPath, box, data, null);
        return m_es.submit(ft);
    }

    
    Object process(EnvelopeCtx ctx, Object data){
        Object res = null;
        if( data instanceof LTRGetStreets){
            LTRGetStreets cmd = (LTRGetStreets) data;
            Street ws = m_rootStreet.queryPath(cmd.getStreetPath());
            res = new ArrayList<String>(ws.getStreets().keySet());

        }
        if( data instanceof LTRAddStreet){
            LTRAddStreet cmd = (LTRAddStreet) data;
            
            cmd.getStreetPath();
            
            Street ws = m_rootStreet;
            
            Street parentStreet = ws.queryPath(cmd.getStreetPath());
            if(null == parentStreet){
                return null;
            }
            
            Street newStreet = new Street(cmd.getBoxes());
            
            parentStreet.getStreets().put(cmd.getStreetName(), newStreet);
            
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
    @SuppressWarnings("unchecked")
    public <T> T call(StreetPath streetPath, String box, Object data) {
        try {
            return (T)post(streetPath, box, data).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
