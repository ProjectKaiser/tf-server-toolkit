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
        Long l = 12234L;
        assertEquals((Integer)10, TFUtils.asInteger(s));
        assertEquals((Integer)10, TFUtils.asInteger("10"));
        assertEquals((Integer)12234, TFUtils.asInteger(l));
    }
    public void testAsLong(){
        Short s = 10;
        assertEquals((Long)10L, TFUtils.asLong(s));
        assertEquals((Long)10L, TFUtils.asLong((Long)10L));
        assertEquals((Long)10L, TFUtils.asLong("10"));
    }
    public void testAsShort(){
        Long l = 12120L;
        Short s =12120;
        assertEquals(s, TFUtils.asShort(l));
        assertEquals((Short)(short)10, TFUtils.asShort("10"));
    }
    

}
