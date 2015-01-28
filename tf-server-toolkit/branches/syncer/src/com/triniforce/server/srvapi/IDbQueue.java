/*
 *
 * (c) Triniforce
 *
 */
package com.triniforce.server.srvapi;

import java.io.Serializable;

/**
 * <h3>Threading issues</h3>
 * Only one thread can read from queue, few threads can write. Queue is notified
 * by notify() method, which wakes up peek() or get(). peek() and get() must
 * immediately check for data and, if none, return null immediately.
 * 
 * <h3>Transaction isues</h3>
 * peek() and get() use current transaction to query queue. If query is empty
 * methods "pop" trasaction before invoking wait() and "push" after invoking
 * wait(), using ISrvSmartTranFactory. Since transaction pop() is invoked, transaction push()
 * must be called immediately before calling to peek()/get() methods.
 * <p>
 * If timeoutMilliseconds is 0 and there is no data method immediately returns,
 * wait() is not called.
 * <p>
 * 
 */

public interface IDbQueue {

    /**
     * 
     * Queries data from queue head, data is not removed form queue. Null if
     * queue is empty.
     * 
     */
    Object peek(long timeoutMilliseconds);

    /**
     * Puts data to queue tail. Queue must NOT be notified since non-commited
     * fantoms problem
     * 
     * @param data
     */
    void put(Serializable data);

    /**
     * 
     * Gets data from queue head, data is removed from queue. Null if queue is
     * empty.
     * 
     */
    Object get(long timeoutMilliseconds);

    void clean();

	long getId();    
   
}
