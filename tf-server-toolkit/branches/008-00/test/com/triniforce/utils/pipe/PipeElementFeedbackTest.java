/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.pipe;

import junit.framework.TestCase;

public class PipeElementFeedbackTest extends TestCase {

    public void testStopProcessing() {
        PipeElementFeedback fb = new PipeElementFeedback();
        assertFalse(fb.isStopped());
        fb.setStopped(true);
        assertTrue(fb.isStopped());
        fb.setStopped(false);
        assertFalse(fb.isStopped());        
    }

}
