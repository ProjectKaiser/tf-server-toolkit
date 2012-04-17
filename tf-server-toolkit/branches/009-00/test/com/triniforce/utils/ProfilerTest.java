/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

import java.util.Collections;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.IProfiler.ActivityStatus;
import com.triniforce.utils.IProfilerStack.PSI;
import com.triniforce.utils.Profiler.Group;
import com.triniforce.utils.Profiler.INanoTimer;
import com.triniforce.utils.Profiler.Item;
import com.triniforce.utils.Profiler.ProfilerStack;

public class ProfilerTest extends TFTestCase {

    private Profiler m_pr;

    protected void setUp() throws Exception {
        super.setUp();
        m_pr = new Profiler();
    }
    
    public void testProfiler(){
        assertEquals(Collections.emptyMap(), m_pr.getStat());
    }
    
    public void testAsString(){
        m_pr.reportItem("Group 1", "item12345678901234567890123456789012345678901234567890123456789012345678901234567890", 235702L, 86468L, false);
        m_pr.reportItem("Group 1", "item2", 2131715902L, 235724L, false);
        m_pr.reportItem("Group 2", "item2", 123434538681L, 12123L, false);
        m_pr.reportItem("Group 1", "item2", 125902450L, 0L, false);
        trace(m_pr.toString());
    }
    
    public void testReportItem(){
        m_pr.reportItem("g1", "i1", 834230L, 939L, false);
        Item item = m_pr.getStat().get("g1").getItem("i1");
        assertNotNull(item);
        assertEquals(834230L - 939L, item.getOwnTime());
        m_pr.reportItem("g1", "i1", 252L, 0L, false);
        item = m_pr.getStat().get("g1").getItem("i1");
        assertEquals(834230L + 252L, item.getTotalTime());
        m_pr.reportItem("g1", "i2", 12341L, 453L, false);
        item = m_pr.getStat().get("g1").getItem("i2");
        assertEquals(12341L, item.getTotalTime());
    }
    
    public void testItem(){
        Item psi = new Profiler.Item("itm1");
        {
            assertEquals("itm1", psi.getName());
            assertEquals(0, psi.getItNum());
            assertEquals(0L, psi.getTotalTime());
            assertEquals(0L, psi.getChildsTime());
            assertEquals(0L, psi.getOwnTime());
        }
        {
            psi.setIt(666832L, 662379L);
            assertEquals(1, psi.getItNum());
            assertEquals(666832L, psi.getTotalTime());
            assertEquals(662379L, psi.getChildsTime());
            assertEquals(666832L-662379L, psi.getOwnTime());
            psi.setIt(41234L, 4123);
            assertEquals(Double.valueOf((666832L+41234L)/2), psi.getAvgTotalTime());
        }
    }
    
    public void testGroup(){
        Group gr = new Profiler.Group("g1");
        {
            assertEquals("g1", gr.getName());
            assertTrue(gr.getItems().isEmpty());
        }
        {//add\get item test
            Item i1 = new Item("itm1");
            i1.setIt(432, 420);
            {
                gr.addItem(i1);
                assertEquals(i1, gr.getItems().toArray()[0]);
            }
            Item i2 = new Item("itm2");
            i2.setIt(320, 100);
            {
                gr.addItem(i2);
                assertEquals(i2, gr.getItems().toArray()[0]);
                assertEquals(i1, gr.getItems().toArray()[1]);
            }
            {
                i1.setIt(300, 50);
                assertTrue(i1.getOwnTime() > i2.getOwnTime());
                assertEquals(i1, gr.getItems().toArray()[0]);
            }
            {
                assertEquals(i1, gr.getItem("itm1"));
            }
        }
    }
    
    public void testClearResult(){
    	m_pr.reportItem("g1", "i1", 48142L, 0L, false);
    	m_pr.reportItem("g1", "i2", 48142L, 0L, false);
    	m_pr.reportItem("g2", "i3", 4834142L, 330L, false);
    	m_pr.reportItem("g2", "i4", 481432L, 33L, false);
    	
    	m_pr.clearResult();
    	
    	assertEquals(Collections.emptyMap(), m_pr.getStat());
    }
    
    public void testRecursion(){
		ProfilerStack prStack = new Profiler.ProfilerStack(m_pr, new INanoTimer(){
			public long get() {
				return System.nanoTime();
			}});
		PSI psi = prStack.getStackItem("testRecursion", "full");
    	factorial(100, prStack);
    	psi.close();
    	
		trace(m_pr.toString());
		
		Group group = m_pr.getStat().get("testRecursion");
		
		assertTrue(group.getItem("full").getTotalTime() >= group.getItem("factorial1").getTotalTime()+group.getItem("factorial2").getTotalTime());
    }

	private int factorial(int i, ProfilerStack prStack) {

		PSI psi = prStack.getStackItem("testRecursion", "factorial1");
			int res;
			if(i==1)
				res = 1;
			else
				res = i * factorial2(i-1, prStack);
		psi.close();
		
		
		return res;
	}

	private int factorial2(int i, ProfilerStack prStack) {

		PSI psi = prStack.getStackItem("testRecursion", "factorial2");
			int res;
			if(i==1)
				res = 1;
			else
				res = i * factorial(i-1, prStack);
		psi.close();
		
		
		return res;
	}
	
	public void testGetSnapshot() {
		IProfiler profiler = new Profiler();
				
		IProfilerStack stack1 = new ProfilerStack(profiler, null);  
		stack1.getStackItem("group1", "item1");
		stack1.getStackItem("group2", "item2");
				
		IProfilerStack stack2 = new ProfilerStack(profiler, null);
		stack2.getStackItem("group3", "item3");
		stack2.getStackItem("group4", "item4", "context4");
		
		IProfilerStack stack3 = new ProfilerStack(profiler, null);
		stack3.getStackItem("group5", "item5");
		stack3.getStackItem("group6", "item6", "context6");
		
		IProfilerStack stack4 = new ProfilerStack(profiler, null);
		stack4.getStackItem("group6", "item6");
		stack4.getStackItem("group7", "item7");
		
		IProfilerStack stack5 = new ProfilerStack(profiler, null);
				
		ActivityStatus status1 = new ActivityStatus(stack1, "idleInfo1\n" +
				"java.io.FileNotFoundException: c:\test.bin (Отказано в доступе)\n" +
"at java.io.FileOutputStream.open(Native Method)\n" +
"at java.io.FileOutputStream.<init>(Unknown Source)\n" +
"at java.io.FileOutputStream.<init>(Unknown Source)\n" +
"at com.triniforce.server.srvapi.license.SignatureTests.testWithBase64(SignatureTests.java:285)\n" +
"at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
"at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)\n" +
"at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)\n"
		
		);
		ActivityStatus status2 = new ActivityStatus(stack2, null);
		ActivityStatus status3 = new ActivityStatus(stack3, "idleInfo3");
		ActivityStatus status4 = new ActivityStatus(stack4, null);
		ActivityStatus status5 = new ActivityStatus(stack5, null);
		
		profiler.updateActivityStatus(1L, status1);
		profiler.updateActivityStatus(2L, status2);
		profiler.updateActivityStatus(3L, status3);
		profiler.updateActivityStatus(4L, status4);
		profiler.updateActivityStatus(5L, status5);
		
		profiler.removeActivityStatus(5L);
		
		trace(profiler.getSnapshot());
	}

}
