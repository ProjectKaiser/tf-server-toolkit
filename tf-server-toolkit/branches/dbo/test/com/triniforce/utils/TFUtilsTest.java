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

}
