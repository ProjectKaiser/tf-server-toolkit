/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

public class OAuthScheme extends AuthScheme{
    private String m_oAuthId;

    public OAuthScheme() {
    }
    
    public OAuthScheme(String id){
        m_oAuthId = id;
    }
    
    public String getOAuthId() {
        return m_oAuthId;
    }

    public void setOAuthId(String oAuthId) {
        m_oAuthId = oAuthId;
    }

}
