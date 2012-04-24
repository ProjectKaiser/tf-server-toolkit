/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.UUID;

import com.triniforce.db.test.TFTestCase;

public class HudsonApiInvTest extends TFTestCase {
    
    public static final String templateJob = "toolkit";
    public static final String baseAddress = "http://localhost:8080/hudson";
    
    @Override
    public void test() throws Exception {
        HudsonApi api = new HudsonApi(baseAddress);
        assertTrue(api.getListOfJobs().contains(templateJob));
    }
    
    public void testCopy(){
        HudsonApi api = new HudsonApi(baseAddress);
        assertTrue(api.getListOfJobs().contains(templateJob));
        String newTaskName = "test_task_" + UUID.randomUUID().toString();
        assertFalse(api.getListOfJobs().contains(newTaskName));
        api.copyJob(templateJob, newTaskName);
        assertTrue(api.getListOfJobs().contains(newTaskName));
        
    }
    

}
