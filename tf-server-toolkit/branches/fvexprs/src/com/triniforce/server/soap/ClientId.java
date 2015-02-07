/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import com.triniforce.server.soap.VObject.IVOSerializable;
import com.triniforce.soap.PropertiesSequence;

@PropertiesSequence( sequence = {"clientId"})
public class ClientId implements IVOSerializable {

    String m_clientId;
    
    public void fromVObject(VObject vObj) {
        setClientId( (String)vObj.getProp(PROP_CLIENT_ID));
    }

    public static final String PROP_CLIENT_ID = "clientId"; //$NON-NLS-1$
    public static final String NEW1 = "new:1";//$NON-NLS-1$
    
    public VObject toVObject() {
        VObject res = new VObject(this);
        res.setProp(PROP_CLIENT_ID, getClientId());
        return res;
    }

    public String getClientId() {
        return m_clientId;
    }

    public void setClientId(String clientId) {
        m_clientId = clientId;
    }

}
