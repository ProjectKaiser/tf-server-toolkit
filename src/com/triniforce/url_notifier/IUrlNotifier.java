/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.url_notifier;

public interface IUrlNotifier {
    /**
     * @param url
     * @param data
     * 
     * for each subscriber 
     *   if free post event and make busy
     *   else if busy putToQueue
     * 
     * putToQueue
     *    if number of events exceeds
     * 
     * 
     * 
     */
    void postEvent(Object url, Object data);
    void subscribe(Object url, IUrlListener data);
}
