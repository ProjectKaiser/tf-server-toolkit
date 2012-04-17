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
		return TFUtils.constructLocale(language, country, variant);
	}
    
    
    public static boolean isEmptyString(String s) {
		return TFUtils.isEmptyString(s);
	}

    public static Short asShort(Object value){
		return TFUtils.asShort(value);
	}
    
    public static Integer asInteger(Object value){
		return TFUtils.asInteger(value);
	}    
    
    public static boolean equals(Object expected, Object actual) {
		return TFUtils.equals(expected, actual);
	}
}
