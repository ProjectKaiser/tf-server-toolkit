/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.utils.pipe;

import java.util.ArrayList;

import junit.framework.TestCase;

public class DataCollectorTest extends TestCase {

    @SuppressWarnings("serial")
    public void testPush() {
        DataCollector dc = new DataCollector();
        assertEquals(new ArrayList<Object>(), dc.getCollectedList());

        ArrayList<Object> testData = new ArrayList<Object>() {
            {
                add("data1");
                add("data11");
                add("dat2");
            }
        };

        PipeElementFeedback fb = new PipeElementFeedback();
        dc.push(testData.get(0), fb);
        assertFalse(fb.isStopped());
        dc.push(testData.get(1), fb);
        assertFalse(fb.isStopped());
        dc.push(testData.get(2), fb);
        assertFalse(fb.isStopped());
        assertEquals(testData, dc.getCollectedList());        

    }
}
