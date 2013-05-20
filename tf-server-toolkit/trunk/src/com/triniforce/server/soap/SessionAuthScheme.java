/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import com.triniforce.soap.PropertiesSequence;


@PropertiesSequence( sequence = {"session"})
public class SessionAuthScheme extends AuthScheme{
    String session;

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
    
    
}
