/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.srvapi;

import com.triniforce.db.dml.ISmartTran;
import com.triniforce.utils.ApiStack;

public interface ISrvSmartTran extends ISmartTran {

    void registerAffectedQueue(IDbQueue fq);
    
    public static class Helper{
        @SuppressWarnings("unchecked")
        public static <T> T instantiateBL(Class<? extends T> cls){
            return ApiStack.getInterface(ISrvSmartTran.class).instantiateBL(cls);
        }
    }

}
