/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.triniforce.postoffice.intf.IPostMaster;
import com.triniforce.postoffice.intf.LTRAddStreetOrBoxes;
import com.triniforce.postoffice.intf.LTRListBoxes;
import com.triniforce.postoffice.intf.LTRListStreets;
import com.triniforce.postoffice.intf.StreetPath;

public class PostMaster implements IPostMaster{
    
    NamedStreets m_rootStreets = new NamedStreets();
    Street m_rootStreet = new Street();
    ExecutorService m_es;
    
    /**
     *  UUID to POBoxWrapper
     */
    Map<UUID, POBoxWrapper> m_boxWrappers = new ConcurrentHashMap();
    
    
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

    
    Object dispatch(EnvelopeCtx ctx, Object data){
        Object res = null;
        if( data instanceof LTRListStreets){
            LTRListStreets cmd = (LTRListStreets) data;
            Street ws = m_rootStreet.queryPath(cmd.getStreetPath());
            res = new ArrayList<String>(ws.getStreets().keySet());

        }
        if( data instanceof LTRAddStreetOrBoxes){
            return LTRAddStreetOrBoxes_handler.process(this, ctx, data);
        }
        if( data instanceof LTRListBoxes){
            return LTRListBoxes_handler.process(this, ctx, data);
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
