/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.utils;

import junit.framework.TestCase;

public class EUtilsTest extends TestCase {

    public void testENotSupportedPrefix() {
        // ENotSupportedPrefix
        try {
            throw new EUtils.ENotSupportedPrefix("prefix");
        } catch (EUtils.ENotSupportedPrefix e) {
            assertTrue(e.getMessage().contains("prefix"));
        }

    }
}
