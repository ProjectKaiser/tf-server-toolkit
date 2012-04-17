/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.utils;

public interface ITime{
    /**
     * @return Same as USystem.currentTimeMillis()
     */
    long currentTimeMillis();
    
    public static class ITimeHelper{
        public static long currentTimeMillis(){
            ITime time = ApiStack.queryInterface(ITime.class);
            if(null != time){
                return time.currentTimeMillis();
            }else{
                return System.currentTimeMillis();
            }
        }
    }
    
}
