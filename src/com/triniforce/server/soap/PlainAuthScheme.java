/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import com.triniforce.soap.PropertiesSequence;


/**
 * Plain auth scheme
 */
@PropertiesSequence(sequence = {"userName", "password"})
public class PlainAuthScheme extends AuthScheme {
    String userName;
    String password;
    
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
