/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.utils;

import java.util.Locale;

import junit.framework.TestCase;

public class TFUtilsTest_common extends TestCase {

	public void testLineSeparator(){
		assertEquals( System.getProperty("line.separator"), TFUtils.lineSeparator());
		
	}
	
    public void testConstrutLocale(){
        assertEquals( Locale.getDefault(), TFUtils.constructLocale(null, null, null));
        assertEquals( new Locale("en", "ca", "1"), TFUtils.constructLocale("en", "ca", "1"));
        assertEquals( new Locale("en", "ru"), TFUtils.constructLocale("en", "ru", null));
        assertEquals( new Locale("fr"), TFUtils.constructLocale("fr", null, null));        
        
        assertFalse(new Locale("en", "ru").equals(TFUtils.constructLocale("en", "ru", "1")));        
    }
    
    public void testIsEmptyString() {
        assertTrue( TFUtils.isEmptyString(null));
        assertTrue( TFUtils.isEmptyString(""));
        assertFalse( TFUtils.isEmptyString(" "));
    }
    
    public void testAsShort(){
        assertEquals(null, TFUtils.asShort(null) );
        Integer i1 = 10;
        Short s1 = TFUtils.asShort(i1);
        assertTrue(10 == s1);
        Short s2 = 20;
        s1 = TFUtils.asShort(s2);
        assertTrue(20 == s1);        
    }
    
    public void testAsInteger(){
        assertEquals(null, TFUtils.asInteger(null) );
        Short s1 = 10;
        Integer i1 = TFUtils.asInteger(s1);
        assertTrue(10 == i1);
        Integer i2 = 20;
        i1 = TFUtils.asInteger(i2);
        assertTrue(20 == i1);        
    }    
    
    public void testEquals(){
        String s1_1 = "s1";
        String s1_2 = "s";
        s1_2 += "1";
        String s2 = "s2";
        assertNotSame(s1_1, s1_2);
        assertTrue( TFUtils.equals(null, null));
        assertFalse( TFUtils.equals(null, s1_1));
        assertFalse( TFUtils.equals(s1_1, null));
        assertTrue( TFUtils.equals(s1_1, s1_2));
        assertTrue( TFUtils.equals(s1_1, s1_1));
        assertFalse( TFUtils.equals(s1_1, s2));
        assertFalse( TFUtils.equals(s1_2, s2));
    }

}
