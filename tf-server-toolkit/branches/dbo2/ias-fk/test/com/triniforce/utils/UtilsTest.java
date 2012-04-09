/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.utils;

import java.util.Locale;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {

    public void testConstrutLocale(){
        assertEquals( Locale.getDefault(), Utils.constructLocale(null, null, null));
        assertEquals( new Locale("en", "ca", "1"), Utils.constructLocale("en", "ca", "1"));
        assertEquals( new Locale("en", "ru"), Utils.constructLocale("en", "ru", null));
        assertEquals( new Locale("fr"), Utils.constructLocale("fr", null, null));        
        
        assertFalse(new Locale("en", "ru").equals(Utils.constructLocale("en", "ru", "1")));        
    }
    
    public void testIsEmptyString() {
        assertTrue( Utils.isEmptyString(null));
        assertTrue( Utils.isEmptyString(""));
        assertFalse( Utils.isEmptyString(" "));
    }
    
    public void testAsShort(){
        assertEquals(null, Utils.asShort(null) );
        Integer i1 = 10;
        Short s1 = Utils.asShort(i1);
        assertTrue(10 == s1);
        Short s2 = 20;
        s1 = Utils.asShort(s2);
        assertTrue(20 == s1);        
    }
    
    public void testAsInteger(){
        assertEquals(null, Utils.asInteger(null) );
        Short s1 = 10;
        Integer i1 = Utils.asInteger(s1);
        assertTrue(10 == i1);
        Integer i2 = 20;
        i1 = Utils.asInteger(i2);
        assertTrue(20 == i1);        
    }    
    
    public void testEquals(){
        String s1_1 = "s1";
        String s1_2 = "s";
        s1_2 += "1";
        String s2 = "s2";
        assertNotSame(s1_1, s1_2);
        assertTrue( Utils.equals(null, null));
        assertFalse( Utils.equals(null, s1_1));
        assertFalse( Utils.equals(s1_1, null));
        assertTrue( Utils.equals(s1_1, s1_2));
        assertTrue( Utils.equals(s1_1, s1_1));
        assertFalse( Utils.equals(s1_1, s2));
        assertFalse( Utils.equals(s1_2, s2));
    }

}
