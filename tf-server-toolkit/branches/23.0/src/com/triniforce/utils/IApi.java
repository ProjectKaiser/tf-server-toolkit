/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.utils;

import java.util.Map;

public interface IApi {    
  
    public static class EInterfaceNotFound extends RuntimeException{
        public EInterfaceNotFound(String intfName) {
            super(intfName);
        }
        private static final long serialVersionUID = 5570633552635814064L;
    } 


    public Map<Class, Object> getImplementors();


    /**
     * @param intf
     * @return implementor of given interface, throws exception if not found
     * @throws EInterfaceNotFound
     */
    <T> T getIntfImplementor(Class intf) throws EInterfaceNotFound;
    
    
    /**
     * @param intf
     * @return null if interface is not found
     */
    <T> T queryIntfImplementor(Class intf);    
}
