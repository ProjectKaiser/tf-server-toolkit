/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

public class PwdSalt {
    protected String m_pwd;
    protected String m_salt;
    public String getSalt() {
        return m_salt;
    }
    public void setSalt(String hash) {
        m_salt = hash;
    }
    public String getPwd() {
        return m_pwd;
    }
    public void setPwd(String pwd) {
        m_pwd = pwd;
    }
}
