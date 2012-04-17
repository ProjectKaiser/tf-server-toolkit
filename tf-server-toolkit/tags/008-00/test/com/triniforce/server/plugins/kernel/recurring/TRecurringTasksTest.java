/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.recurring;

import com.triniforce.db.dml.ISmartTran;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IBasicServer.Mode;

public class TRecurringTasksTest extends BasicServerTestCase{
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		getServer().enterMode(Mode.Running);
	}
	
    @Override
    protected void tearDown() throws Exception {
        getServer().leaveMode();
        super.tearDown();
    }
    
    @Override
    public void test() throws Exception {
        TRecurringTasks.BL bl = ISmartTran.Helper.instantiateBL(TRecurringTasks.BL.class); 
        bl.deleteAll();
        assertNull(bl.selectFirst());
        bl.insert(111, 222, 333, 444);
        bl.insert(11, 22, 33, 44);
        bl.insert(1, 2, 3, 4);
        
        {
            TRecurringTasks.Data data = bl.selectFirst();
            assertEquals(1, data.id);
            assertEquals(2, data.extension_id);
            assertEquals(3, data.start);
            assertEquals(4, data.past_threshold);
        }
        
        bl.delete(1);
        {
            TRecurringTasks.Data data = bl.selectFirst();
            assertEquals(11, data.id);
            assertEquals(22, data.extension_id);
            assertEquals(33, data.start);
            assertEquals(44, data.past_threshold);
        }
        bl.delete(11);
        {
            TRecurringTasks.Data data = bl.selectFirst();
            assertEquals(111, data.id);
            assertEquals(222, data.extension_id);
            assertEquals(333, data.start);
            assertEquals(444, data.past_threshold);
        }
        
    }
}
