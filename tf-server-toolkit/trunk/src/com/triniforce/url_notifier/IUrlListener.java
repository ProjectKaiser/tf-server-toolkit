/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.url_notifier;

import java.util.Collection;

public interface IUrlListener{
    void reset(Object key);
    void handleEvents(Object key, Collection<Object> events, int missedEvents);
}
