/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.core;

import java.util.concurrent.Future;

public class PostMaster implements IPostMaster{
    
    
    protected class PostTask implements Runnable{
        private final Object m_data;
        private final IEnvelopeHandler m_replyHandler;
        private final StreetPath m_streetPath;
        private final String m_box;

        public PostTask(StreetPath streetPath, String box, Object data, IEnvelopeHandler replyHandler) {
            m_streetPath = streetPath;
            m_box = box;
            m_data = data;
            m_replyHandler = replyHandler;
        }


        public Object getData() {
            return m_data;
        }

        public IEnvelopeHandler getReplyHandler() {
            return m_replyHandler;
        }

        public void run(){
            
        }
        public StreetPath getStreetPath() {
            return m_streetPath;
        }
        public String getBox() {
            return m_box;
        }
        
    }
    
    
    public Future post(StreetPath streetPath, String box, Object data){
        return null;
    }

    public Future post(StreetPath streetPath, Class addr, Object data) {
        return post(streetPath, addr.getName(), data);
    }

}
