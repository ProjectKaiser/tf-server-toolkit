/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import java.util.Collection;

public interface ISubscriber{
    void reset(Object key);
    void handleEvents(Object key, Collection<Object> events, int missedEvents);
}
