/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import com.triniforce.db.test.TFTestCase;

public class TFUtilsTest extends TFTestCase {
    public static final String UNICODE_PATTERN = "۞∑русскийڧüöäë面伴";
    @Override
    public void test() throws Exception {
        String us = TFUtils.readResource(this.getClass(), "TFUtilsTest_unicode.txt");
        assertEquals(UNICODE_PATTERN, us);
    }
    
    public void testToString(){
    	assertEquals("null", TFUtils.toString(null));
    	assertEquals("5", TFUtils.toString(new Integer(5)));
    }
    
    public void testAsInteger(){
        Short s = 10;
        assertEquals((Integer)10, TFUtils.asInteger(s));
        assertEquals((Integer)10, TFUtils.asInteger("10"));
    }

}
