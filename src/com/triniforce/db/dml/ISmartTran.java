/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.db.dml;


public interface ISmartTran extends IStmtContainer {

    /**
     * Mark transaction to be commited 
     */
    void commit();
    
    void close(boolean bCommit);
    
    /**
     * Transaction is not commited even if commit() method was called 
     */
    void doNotCommit();    
    
    /**
     * @return true is transaction will be commited, false otherwise. Transaction will be 
     * commited if commit() has been called and doNotCommit() has not been called.
     */
    boolean toBeCommited();
    
    boolean isCommited();

}