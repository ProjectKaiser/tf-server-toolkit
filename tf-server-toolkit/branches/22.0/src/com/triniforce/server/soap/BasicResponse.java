/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.soap.PropertiesSequence;

@PropertiesSequence( sequence = {"headers", "sourceStatus"})
public class BasicResponse {
    Map<String, Object> m_headers = new HashMap<String, Object>();    
    
    public Map<String, Object> getHeaders() {
        return m_headers;
    }
    
    public void setHeaders(Map<String, Object> headers){
        m_headers = headers;
    }
    
    /**
     * 0 source not exists. ( used by flatviews )
     * 1 ok
     * 2 sourse is busy, try later (used by flatviews)
     */
    int m_SourceStatus = 1;

    public int getSourceStatus() {
        return m_SourceStatus;
    }

    public void setSourceStatus(int status){
        m_SourceStatus = status;
    }
    
    
    
}
