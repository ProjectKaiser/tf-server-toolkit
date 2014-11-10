/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

public class GoogleTokenScheme extends AuthScheme{
    private String m_token;
    private String m_email;
    private String m_pictureUrl;
    private String m_displayName;
    public String getToken() {
        return m_token;
    }
    public void setToken(String token) {
        m_token = token;
    }
    public String getEmail() {
        return m_email;
    }
    public void setEmail(String email) {
        m_email = email;
    }
    public String getPictureUrl() {
        return m_pictureUrl;
    }
    public void setPictureUrl(String pictureUrl) {
        m_pictureUrl = pictureUrl;
    }
    public String getDisplayName() {
        return m_displayName;
    }
    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }
}
