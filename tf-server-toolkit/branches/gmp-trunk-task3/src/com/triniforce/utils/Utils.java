/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.utils;

import java.util.Locale;

public class Utils {    

    /**
     * @param language
     * @param country
     * @param variant
     * @return Locale.getDefault() if all parameters are null
     */
    public static Locale constructLocale(Object language, Object country, Object variant){
        if( language != null && country != null &&  variant != null){
            return new Locale((String)language, (String)country, (String)variant);            
        }
        if( language != null && country != null){
            return new Locale((String)language, (String)country);            
        }
        if( language != null){
            return new Locale((String)language);            
        }        
        return Locale.getDefault();        
    }
    
    
    public static boolean isEmptyString(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        return false;
    }

    public static Short asShort(Object value){
        if( null == value )return null;
        if( value instanceof Integer){
            return (short)(int)(Integer) value;
        }
        return (Short)value;
    }
    
    public static boolean equals(Object expected, Object actual) {
        if (expected == actual)
            return true;
        if (expected == null || actual == null)
            return false;
        return expected.equals(actual);
    }
}
