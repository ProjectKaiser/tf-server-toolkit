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
    public static final String templateJobParams = "toolkit-with-params";
    public static final String baseAddress = "http://localhost:8080/hudson";
    
    public static final String user = "admin";
    public static final String pwd = "q";
    
    @Override
    public void test() throws Exception {
        HudsonApi api = new HudsonApi(baseAddress, user, pwd);
        assertTrue(api.getListOfJobs().contains(templateJob));
    }
    
    public void testCopy(){
        HudsonApi api = new HudsonApi(baseAddress, user, pwd);
        assertTrue(api.getListOfJobs().contains(templateJob));
        String newTaskName = "test_task_" + UUID.randomUUID().toString();
        assertFalse(api.getListOfJobs().contains(newTaskName));
        api.copyJob(templateJob, newTaskName);
        assertTrue(api.getListOfJobs().contains(newTaskName));
    }
    
    public void test_getJobConfigXml(){
        HudsonApi api = new HudsonApi(baseAddress, user, pwd);
        assertTrue(api.getListOfJobs().contains(templateJob));
        assertTrue(api.getJobConfigXml(templateJob).contains("toolkit"));
    }
    
    public void test_createJob(){
        HudsonApi api = new HudsonApi(baseAddress, user, pwd);
        String newTaskName = "test_updateJobConfigXml" + UUID.randomUUID().toString();
        
        String config = api.getJobConfigXml(templateJobParams);
        config = config.replace("12.34", "56.78");
        config = config.replace("trunk", "branches/010-00");
        api.createJob(newTaskName, config);
    }    

}
