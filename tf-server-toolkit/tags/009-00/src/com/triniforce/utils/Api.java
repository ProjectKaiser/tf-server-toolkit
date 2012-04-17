/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class Api implements IApi, IFinitable {

    protected Map<Class, Object> m_map = new LinkedHashMap<Class, Object>();
    
    public Api() {
    }
    
    public Api(Class intf, Object implementor) {
        setIntfImplementor(intf, implementor);
    }
    
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public <T> T getIntfImplementor(Class cls) throws EInterfaceNotFound {
        Object result = queryIntfImplementor(cls);
        if (null == result) {
            throw new EInterfaceNotFound(cls.getName());
        }
        return (T)result;
    }

    public Object setIntfImplementor(Class intf, Object implementor) {
        Object result = queryIntfImplementor(intf);
        m_map.put(intf, implementor);
        return result;
    }
    
    public Object clearIntfImplementor(Class intf) {
        Object result = queryIntfImplementor(intf);
        m_map.remove(intf);
        return result;
    }    

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public <T> T queryIntfImplementor(Class intf) {
        return (T)m_map.get(intf);
    }

    public void finit() {
    	Object[] arr = m_map.values().toArray();
    	for(int i=arr.length-1; i>=0; i--){
    		Object obj = arr[i];
    		if(obj instanceof IFinitable)
            try {
                // finit element
            	IFinitable finitable = (IFinitable) obj;
            	finitable.finit();
            } catch (Throwable t) {
                ApiAlgs.getLog(this).error("Api finit problem", t);//$NON-NLS-1$
            }
        }
    }

	public Map<Class, Object> getImplementors() {
		return m_map;
	}

}
