/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.EUtils.EAssertEqualsFailed;

public class TFUtilsTest extends TFTestCase {
    public static final String UNICODE_PATTERN = "۞∑русскийڧüöäë面伴";
    
    public void test_coalesce(){
    	Integer iTest = 1234455;
    	assertEquals("", TFUtils.coalesce(null, ""));
    	assertEquals("a", TFUtils.coalesce(null, "a"));
    	assertEquals(iTest, TFUtils.coalesce(null, iTest));
    	assertEquals(null, TFUtils.coalesce(null, null));
    	assertEquals("aa", TFUtils.coalesce("aa", null));
    	assertEquals("", TFUtils.coalesce("", "bb"));
    }
    
    public void test_coalesceString(){
    	assertEquals("", TFUtils.coalesceString(null, ""));
    	assertEquals("a", TFUtils.coalesceString(null, "a"));
    	assertEquals(null, TFUtils.coalesceString(null, null));
    	assertEquals("aa", TFUtils.coalesceString("aa", null));
    	assertEquals("bb", TFUtils.coalesceString("", "bb"));
    }
    
    public void test_firstMatchedGroups(){
    	assertEquals(0, TFUtils.firstMatchedGroups(null, null).length);
    	assertEquals(0, TFUtils.firstMatchedGroups("", null).length);
    	assertEquals(0, TFUtils.firstMatchedGroups(null, "").length);
    	assertEquals(0, TFUtils.firstMatchedGroups(null, "").length);
    	
    	assertEquals(Arrays.asList("t"), Arrays.asList(TFUtils.firstMatchedGroups("text", "t")));
    	assertEquals(Arrays.asList("=?UTF-8?B?", "UTF-8"), Arrays.asList(TFUtils.firstMatchedGroups(" =?UTF-8?B?0JHQu9Cw0L3QuiDQt9Cw0Y", "=\\?(UTF-8)\\?B\\?")));
    	assertEquals(Arrays.asList("=?koi8-r?B?", "koi8-r"), Arrays.asList(TFUtils.firstMatchedGroups("=?koi8-r?B?0JHQu9Cw0L3QuiDQt9Cw0Y", "^=\\?([^?]*)\\?B\\?")));
    	
    }
    
    public void test_string_bytes(){
    	assertNull(TFUtils.stringToBytes(null));
    	assertNull(TFUtils.bytesToString(null));
    	
    	byte bytes[] = TFUtils.stringToBytes(UNICODE_PATTERN);
    	String str = TFUtils.bytesToString(bytes);
    	assertEquals(UNICODE_PATTERN, str);
    }
    
    
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
        assertEquals((Long)10L, TFUtils.asLong(10L));
        assertEquals((Long)10L, TFUtils.asLong("10"));
    }
    public void testAsShort(){
        Long l = 12120L;
        Short s =12120;
        assertEquals(s, TFUtils.asShort(l));
        assertEquals((Short)(short)10, TFUtils.asShort("10"));
    }
    
	public void testAssertParams() {
		TFUtils.assertParams(1, 2).equal(1, 2);
		TFUtils.assertParams(1, 2L).equal(1, (short) 2);
		TFUtils.assertParams(1, 2, 3).equal(1, "2", 3);

		// 2 == "2" but not vice versa
		try {
			TFUtils.assertParams(1, "2").equal(1, 2);
			fail();
		} catch (EAssertEqualsFailed e) {
		}

		// Array vs list
		{
			TFUtils.assertParams(1, 2).equal(Arrays.asList(new Object[] { 1, 2 }));
			try {
				TFUtils.assertParams(1, 2, 3).equal(Arrays.asList(new Object[] { 1, 2 }));
				fail();
			} catch (EAssertEqualsFailed e) {
			}
		}

		// List vs Array
		{
			TFUtils.assertParams(Arrays.asList(new Object[] { 1, 2 })).equal(1, 2);

			try {
				TFUtils.assertParams(Arrays.asList(new Object[] { 1, 2 })).equal(1, 2, 3);
				fail();
			} catch (EAssertEqualsFailed e) {
			}
		}

	}
    
    public void testEquals(){
    	
    	try{
    		TFUtils.assertEquals(1, "aaa");
    		fail();
    	}catch(EUtils.EAssertEqualsFailed e){
    		trace(e);
    	}
    	
		// Array vs not array
		try {
			TFUtils.assertEquals(new Object[] { 1, 2, 3 }, "aaa");
			fail();
		} catch (EUtils.EAssertEqualsFailed e) {
			trace(e);
		}

		// Different size
		try {
			TFUtils.assertEquals(new Object[] { 1, 2, 3 }, new Object[] { 1, 2 });
			fail();
		} catch (EUtils.EAssertEqualsFailed e) {
			trace(e);
		}
		
		// Different [1][1]
		try {
			TFUtils.assertEquals(new Object[] { 1, new Object[]{1, 2}}, new Object[] { 1, new Object[]{1, 3} });
			fail();
		} catch (EUtils.EAssertEqualsFailed e) {
			trace(e);
		}
		
		// Different [1].length
		try {
			TFUtils.assertEquals(new Object[] { 1, new Object[]{1, 2}}, new Object[] { 1, new Object[]{1, 3, 3} });
			fail();
		} catch (EUtils.EAssertEqualsFailed e) {
			trace(e);
		}
		
		
		// Empty arrays differ
		TFUtils.assertEquals(new Object[] { 1, new Object[]{}}, new Object[] { 1, new Object[]{} });
		
		// String equals Integer
		TFUtils.assertEquals(new Object[] { 1, 2, 3 }, new Object[] { 1, "2", 3 });
		
		// Integer are same
		TFUtils.assertEquals(new Object[] { 1, 2L, (short)(3)}, new Object[] { (short)1, 2, 3L});
		
		// List and array same
		
		TFUtils.assertEquals(new Object[] { 1, 2L, (short)(3)}, Arrays.asList(new Object[] { (short)1, 2, 3L}));
		
		
    }
    
    public void testFilePrintRead() throws IOException{
        File tmp = getTmpFolder(this);
        File txt = new File(tmp, "txt");
        List<String> res;
        
        
        //empty
        {
            txt.delete();
            txt.createNewFile();
            res = TFUtils.readLinesFromFile(txt, 0);
            assertEquals(0, res.size());
            res = TFUtils.readLinesFromFile(txt, 1);
            assertEquals(0, res.size());
        }
        
        //single line
        {
            txt.delete();
            TFUtils.printlnToFile(txt, "1");
            res = TFUtils.readLinesFromFile(txt, 0);
            assertEquals(0, res.size());
            res = TFUtils.readLinesFromFile(txt, 1);
            assertEquals(1, res.size());
            assertEquals("1", res.get(0));
        }
        
        //two lines
        {
            txt.delete();
            TFUtils.printlnToFile(txt, "1");
            TFUtils.printlnToFile(txt, "2", true);
            res = TFUtils.readLinesFromFile(txt, 0);
            assertEquals(0, res.size());
            res = TFUtils.readLinesFromFile(txt, 1);
            assertEquals(1, res.size());
            assertEquals("1", res.get(0));
            res = TFUtils.readLinesFromFile(txt, 2);
            assertEquals(2, res.size());
            assertEquals("1", res.get(0));
            assertEquals("2", res.get(1));
            res = TFUtils.readLinesFromFile(txt, 4);
            assertEquals(2, res.size());
            assertEquals("1", res.get(0));
            assertEquals("2", res.get(1));                        
        }
    }
    
    public void testReadLastLinesFromFile() throws IOException{
        File tmp = getTmpFolder(this);
        File txt = new File(tmp, "txt");
        List<String> res;
        
        //empty
        {
            txt.delete();
            txt.createNewFile();
            res = TFUtils.readLastLinesFromFile(txt, 1, 80);
            assertEquals(0, res.size());
            res = TFUtils.readLastLinesFromFile(txt, 0, 80);
            assertEquals(0, res.size());            
        }
        //one line
        {
            txt.delete();
            TFUtils.printlnToFile(txt, "1");
            res = TFUtils.readLastLinesFromFile(txt, 1, 80);
            assertEquals(1, res.size());
            assertEquals("1", res.get(0));
            
            res = TFUtils.readLastLinesFromFile(txt, 4, 80);
            assertEquals(1, res.size());
            assertEquals("1", res.get(0));            
            
            res = TFUtils.readLastLinesFromFile(txt, 0, 80);
            assertEquals(0, res.size());
        }
        //three lines
        {
            txt.delete();
            TFUtils.printlnToFile(txt, "1");
            TFUtils.printlnToFile(txt, "2", true);
            TFUtils.printlnToFile(txt, "3", true);
            res = TFUtils.readLastLinesFromFile(txt, 1, 80);
            assertEquals(1, res.size());
            assertEquals("3", res.get(0));
            
            res = TFUtils.readLastLinesFromFile(txt, 2, 80);
            assertEquals(2, res.size());
            assertEquals("2", res.get(0));
            assertEquals("3", res.get(1));            
        }
        //long first line
        {
            txt.delete();
            TFUtils.printlnToFile(txt, "1-0123456789qweoquwyeoiquwy eoiquweyqowieu oqwue hqklwjeh qlwjkehq lwjkeh");
            TFUtils.printlnToFile(txt, "2", true);
            TFUtils.printlnToFile(txt, "3-0123456789", true);
            
            res = TFUtils.readLastLinesFromFile(txt, 2, 1);
            assertEquals(2, res.size());
            assertEquals("2", res.get(0));
            
            res = TFUtils.readLastLinesFromFile(txt, 3, 2);
            assertEquals(2, res.size());
            assertEquals("2", res.get(0));
        }
        
        //long last line
        {
            txt.delete();
            TFUtils.printlnToFile(txt, "2", true);
            TFUtils.printlnToFile(txt, "3-0123456789", true);

            final String longLine = "1-0123456789qweoquwyeoiquwy eoiquweyqowieu oqwue hqklwjeh qlwjkehq lwjkeh"; 
            TFUtils.printlnToFile(txt, longLine, true);
            
            res = TFUtils.readLastLinesFromFile(txt, 1, 1);
            assertEquals(1, res.size());
        }        
        
    }
    

}
