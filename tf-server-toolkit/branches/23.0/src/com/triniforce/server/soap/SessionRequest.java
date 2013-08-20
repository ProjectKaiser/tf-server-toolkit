/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import com.triniforce.soap.PropertiesSequence;


@PropertiesSequence( sequence = {"scheme"})
public class SessionRequest extends BasicRequest {
    /**
     * Can be instance of either PlainAuthScheme or SessionAuthScheme
     */
    AuthScheme m_scheme;

    public AuthScheme getScheme() {
        return m_scheme;
    }

    public void setScheme(AuthScheme scheme) {
        m_scheme = scheme;
    }
}
