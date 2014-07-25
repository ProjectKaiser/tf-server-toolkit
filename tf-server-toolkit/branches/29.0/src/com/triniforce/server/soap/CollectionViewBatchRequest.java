/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.List;

public class CollectionViewBatchRequest extends SessionRequest{
    
    static final long DEFAULT_MAX_EXECUTION_TIME_MS = 300L;

    List<CollectionViewRequest> m_requests = new ArrayList<CollectionViewRequest>();
    long m_maxExecutionTimeMs = DEFAULT_MAX_EXECUTION_TIME_MS;

    public List<CollectionViewRequest> getRequests() {
        return m_requests;
    }

    public void setRequests(List<CollectionViewRequest> requests) {
        m_requests = requests;
    }

    public long getMaxExecutionTimeMs() {
        return m_maxExecutionTimeMs;
    }

    public void setMaxExecutionTimeMs(long maxExecutionTimeMs) {
        m_maxExecutionTimeMs = maxExecutionTimeMs;
    }
    
    
}
