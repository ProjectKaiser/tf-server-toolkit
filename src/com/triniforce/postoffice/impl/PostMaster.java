/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

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

/**
 * https://docs.google.com/drawings/d/1dnYlLayiyt1PGgCImpt86lBIBvA7LKQAjQbIiOozjBE/edit
 */
public class PostMaster implements IPostMaster{
    
    NamedStreets m_rootStreets = new NamedStreets();
    Street m_rootStreet = new Street();
    ExecutorService m_es;
    
    /**
     *  UUID to POBoxWrapper
     */
    Map<UUID, POBoxWrapper> m_boxWrappers = new ConcurrentHashMap();
    
    /*
     * 
     *  PUBLIC METHODS
     * 
     * 
     */
    
    public PostMaster() {
        this(Executors.newFixedThreadPool(20));
    }
    public PostMaster(ExecutorService es) {
        m_es = es;
        m_rootStreets.put(IPostMaster.ROOT_STREET, m_rootStreet);
    }
    
    public Future post(StreetPath streetPath, String box, Object data){
        PostTask ft = new PostTask(this, null, null, streetPath, box, data, null);
        return m_es.submit(ft);
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
    
    
    /*
     * 
     * HELPERS
     * 
     * 
     */
    
    public POBoxWrapper queryTargetBox(POBoxWrapper sender, StreetPath targetStreetPath, String targetBox){
        Street ws = null == sender? m_rootStreet: sender.getParent();
        
        if(null == targetStreetPath){
            POBoxWrapper res = null;
            Street parentStreet = sender.getParent();
            while(null != parentStreet){
                res = parentStreet.getBoxes().get(targetBox);
                if(null != res){
                    return res;
                }else{
                    parentStreet = parentStreet.getParent();
                    return res;
                }
            }
            return null;
        }else{
            Street targetStreet =  ws.queryPath(targetStreetPath);
            if(null == targetStreet){
                return null;
            }
            return targetStreet.getBoxes().get(targetBox);
        }
    }
    
    protected Object process(EnvelopeCtx ctx, Object data, Outboxes outs){
        Object res = null;
        if( data instanceof LTRListStreets){
            return LTRListStreets_handler.process(this, ctx, (LTRListStreets) data);
        }
        if( data instanceof LTRListBoxes){
            return LTRListBoxes_handler.process(this, ctx, (LTRListBoxes) data);
        }
        if( data instanceof LTRAddStreetOrBoxes){
            return LTRAddStreetOrBoxes_handler.process(this, ctx, (LTRAddStreetOrBoxes) data, outs);
        }
        return res;
    }
    


}
