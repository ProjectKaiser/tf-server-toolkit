/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.List;

public class CollectionViewBatchResponse {

    List<LongListResponse> m_responses = new ArrayList<LongListResponse>();

    public List<LongListResponse> getResponses() {
        return m_responses;
    }

    public void setResponses(List<LongListResponse> responses) {
        m_responses = responses;
    }
    
}
