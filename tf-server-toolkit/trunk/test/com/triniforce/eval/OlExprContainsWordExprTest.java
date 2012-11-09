/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.triniforce.db.test.TFTestCase;

public class OlExprContainsWordExprTest extends TFTestCase {
    
    public void testPattern(){
        Pattern pattern = Pattern.compile("(^|[\\s.,;\\-]+)\\Р\\а\\б\\о\\т\\а".toLowerCase(), Pattern.CASE_INSENSITIVE);
        final String s1 = "Работа";
        final String s2 = "The    Работа23";
        final String s3 = "The Рбота23";
        final String s4 = "The моя-Работа23";
        final String s5 = "The моя Рабо   .Рабо.          Рабо Работа23";
        {
            Matcher matcher = pattern.matcher(s1.toLowerCase());
            assertTrue(matcher.find());
        }
        {
            Matcher matcher = pattern.matcher(s2.toLowerCase());
            assertTrue(matcher.find());
        }
        {
            Matcher matcher = pattern.matcher(s3.toLowerCase());
            assertFalse(matcher.find());
        }
        {
            Matcher matcher = pattern.matcher(s4.toLowerCase());
            assertTrue(matcher.find());
        }
        {
            Matcher matcher = pattern.matcher(s5.toLowerCase());
            assertTrue(matcher.find());
        }
        Long start = System.currentTimeMillis();
        for(int i = 0;i < 1000; i++){
            Matcher matcher = pattern.matcher(s5.toLowerCase());
            matcher.find();
        }
        trace("Spent time: " + (System.currentTimeMillis() - start));
        
    }
    
    @Override
    public void test() throws Exception {
    }   

}
