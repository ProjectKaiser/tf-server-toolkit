/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.srvapi;

import java.util.List;

import com.triniforce.utils.ApiStack;

public interface ISrvSmartTranFactory extends IModeAny {

    public static class Helper {
        public static void push() {
            ISrvSmartTranFactory tf = ApiStack
                    .getInterface(ISrvSmartTranFactory.class);
            tf.push();
        }

        public static void commit() {
            ISrvSmartTran tran = ApiStack
                    .getInterface(ISrvSmartTran.class);
            tran.commit();
        }
        
        public static void pop() {
            ISrvSmartTranFactory tf = ApiStack
                    .getInterface(ISrvSmartTranFactory.class);
            tf.pop();
        }
        public static void commitAndStartTran() {
            ISrvSmartTranFactory.Helper.commit();
            ISrvSmartTranFactory.Helper.pop();
            ISrvSmartTranFactory.Helper.push();
        }        
    }

    /**
     * Get connection from connection pool and put to api stack ISrvSmartTran
     * interface implementation
     */
    void push();

    /**
     * Closes previously installed ISrvSmartTran and removes it from api stack
     */
    void pop();
    
    public interface ITranExtender{
        public void push();
        public void pop(boolean bCommit);        
    }
    
    /**
     * Is invoked before ISrvSmartTran is installed and after it is removed
     */
    void registerOuterExtender(ITranExtender interceptor);
    
    /**
     * Is invoked after ISrvSmartTran is installed and before it is removed
     */    
    void registerInnerExtender(ITranExtender interceptor);
    
    /**
     * @return registered outter trasaction extenders
     */
    List<ITranExtender> getOuterExtenders();
    
    /**
     * @return registered inner transaction extenders
     */
    List<ITranExtender> getInnerExtenders();
    
}
