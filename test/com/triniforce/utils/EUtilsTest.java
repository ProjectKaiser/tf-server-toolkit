/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.utils;

import java.io.IOException;

import com.triniforce.utils.ApiAlgs.RethrownException;

import junit.framework.TestCase;

public class EUtilsTest extends TestCase {

	public void testCause() {
		assertNull(EUtils.unwrap(null));
		{
			RuntimeException rt = new RuntimeException("");
			assertSame(rt, EUtils.unwrap(rt));
		}
		{
			IOException io = new IOException("");
			RuntimeException rt = new RuntimeException(io);
			assertSame(io, EUtils.unwrap(rt));
		}		
		
		{
			IOException io = new IOException("");
			RuntimeException rt1 = new RuntimeException(io);
			RuntimeException rt2 = new RuntimeException(rt1);
			assertSame(io, EUtils.unwrap(rt2));
		}
		
		{
			IOException io = new IOException("");
			RuntimeException rt1 = new RuntimeException(io);
			RethrownException ret = new RethrownException(rt1);
			assertSame(io, EUtils.unwrap(ret));
		}
	}
	
    public void testENotSupportedPrefix() {
        // ENotSupportedPrefix
        try {
            throw new EUtils.ENotSupportedPrefix("prefix");
        } catch (EUtils.ENotSupportedPrefix e) {
            assertTrue(e.getMessage().contains("prefix"));
        }
    }
}
