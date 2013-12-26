/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.StringSerializer;

@PropertiesSequence( sequence = {"headers"})
public class BasicRequest {
    Map<String, Object> m_headers = new HashMap<String, Object>();
    public Map<String, Object> getHeaders() {
        return m_headers;
    }    
    
    public void setHeaders(Map<String, Object> headers){
        m_headers = headers;
    }
    
    @Override
    public String toString() {
        return StringSerializer.Object2JSON(this);
    }
    
}
