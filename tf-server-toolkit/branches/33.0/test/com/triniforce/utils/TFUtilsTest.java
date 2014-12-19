/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

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