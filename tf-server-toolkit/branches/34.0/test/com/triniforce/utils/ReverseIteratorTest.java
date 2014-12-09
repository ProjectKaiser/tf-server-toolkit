/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.utils;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.test.TFTestCase;

public class ReverseIteratorTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        List<String> myList = new ArrayList<String>();
        myList.add("str1");
        myList.add("str2");
        myList.add("str3");
        myList.add("stra");
        myList.add("str1");

        {
            int i = myList.size() - 1;
            for (String s : new ReverseIterator<String>(myList)) {
                assertEquals(myList.get(i), s);
                i--;
            }
            assertEquals(-1, i);
        }

        //use same iterator twice
        {
            int i = myList.size() - 1;
            ReverseIterator<String> ri = new ReverseIterator<String>(myList);
            for (String s : ri) {
                assertEquals(myList.get(i), s);
                i--;
            }
            assertEquals(-1, i);
            
            i = myList.size() - 1;
            for (String s : ri) {
                assertEquals(myList.get(i), s);
                i--;
            }
            assertEquals(myList.size() - 1, i);
        }

    }
}
