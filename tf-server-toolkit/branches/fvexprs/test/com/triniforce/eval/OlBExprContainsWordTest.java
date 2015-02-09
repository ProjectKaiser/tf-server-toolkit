/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.eval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.triniforce.db.test.TFTestCase;

public class OlBExprContainsWordTest extends TFTestCase {
    
    public void testPattern(){
        Pattern pattern = Pattern.compile("(^|[\\s.,;\\-]+)Р\\а\\б\\о\\т\\а".toLowerCase(), Pattern.CASE_INSENSITIVE);
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
    
    public void testColumns(){
        OlEval of = new OlEval();
        of.addExpr(0, new OlBExprContainsWord( new OlExprColumn(1)));
        assertTrue(of.evalArray(new Object[]{"1", "1"}, 0));
        assertFalse(of.evalArray(new Object[]{"1", "2"}, 0));
        assertTrue(of.evalArray(new Object[]{"2 1", "1"}, 0));
        assertTrue(of.evalArray(new Object[]{"2 1235", "123"}, 0));
        assertFalse(of.evalArray(new Object[]{null, "1"}, 0));
        assertFalse(of.evalArray(new Object[]{"2 1", null}, 0));
        assertFalse(of.evalArray(new Object[]{null, null}, 0));
    }
    
    @Override
    public void test() throws Exception {
        {
            OlEval of = new OlEval();
            of.addExpr(0, new OlBExprContainsWord("tHe"));
            assertFalse(of.evalArray(new Object[]{null}, 0));
            assertTrue(of.evalArray(new Object[]{"ThE"}, 0));
            assertTrue(of.evalArray(new Object[]{" the"}, 0));
            assertTrue(of.evalArray(new Object[]{"crop.the"}, 0));
            assertFalse(of.evalArray(new Object[]{"crop.notthe"}, 0));
            assertFalse(of.evalArray(new Object[]{"  notthe"}, 0));
        }
        {
            OlEval of = new OlEval();
            of.addExpr(0, new OlBExprContainsWord("maxim.ge"));
            assertFalse(of.evalArray(new Object[]{null}, 0));
            assertFalse(of.evalArray(new Object[]{"ThEmaxim.ge@gmail.com"}, 0));
            assertTrue(of.evalArray(new Object[]{"maxim.ge@gmail.com"}, 0));
        }
        {
            OlEval of = new OlEval();
            of.addExpr(0, new OlBExprContainsWord("gMail.com"));
            assertFalse(of.evalArray(new Object[]{null}, 0));
            assertFalse(of.evalArray(new Object[]{"maxim.ge@_gmail.com"}, 0));
            assertTrue(of.evalArray(new Object[]{"maxim.ge@gmAil.com"}, 0));
        }
        {
            OlEval of = new OlEval();
            of.addExpr(0, new OlBExprContainsWord("maxim.ge@gmail.com"));
            assertFalse(of.evalArray(new Object[]{null}, 0));
            assertFalse(of.evalArray(new Object[]{"maxim.ge@_gmail.com"}, 0));
            assertTrue(of.evalArray(new Object[]{"a maxim.ge@gmAil.com"}, 0));
        }
    }   

}
