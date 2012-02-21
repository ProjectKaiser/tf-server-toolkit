/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.utils;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.EmptyStackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.server.plugins.kernel.ServerImplTest;
import com.triniforce.utils.IProfiler;
import com.triniforce.utils.IProfilerStack;
import com.triniforce.utils.Profiler;
import com.triniforce.utils.IProfilerStack.PSI;
import com.triniforce.utils.Profiler.INanoTimer;
import com.triniforce.utils.Profiler.Item;

import junit.framework.TestCase;

public class ProfilerStackTest extends TestCase implements IProfiler, INanoTimer{

    private IProfilerStack st;
    private ArrayList<TestItem> m_reported;
    //private SrvApiEmu m_timer;
	private TstTimer m_timer;
    
    static class TestItem  extends Item{

        private String m_group;

        public TestItem(String group, String name, long total, long child) {
            super(name);
            setIt(total, child);
            m_group = group;
        }
        
        public TestItem(String group, String name) {
            this(group, name, 0L, 0L);
        }

        String getGroup(){
            return m_group;
        }
        
    }
    
    static class TstTimer implements ITime{
    	int offs[] = {20,10,40,30};
    	int id=0;
    	long sum;

		public long currentTimeMillis() {
			long res = sum;
			sum += offs[id%4];
			id++;
			return res;
		}

		public int getTimeMillis(int i) {
			int res = (i/4)*100;
			for(int j=0; j<i%4; j++)
				res += offs[j];
			return res;
		}

		public int currentTimerId() {
			return id;
		}
    	
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        st = new Profiler.ProfilerStack(this, this);
        m_reported = new ArrayList<TestItem>();
        m_timer = new TstTimer();
    }
    
    
    public void testGetItem(){
        {
            PSI psi = st.getStackItem("group1", "item1", "context");        
            assertNotNull(psi);
            assertEquals("group1", psi.getGroup());
            assertEquals("item1", psi.getItem());
            assertEquals(m_timer.getTimeMillis(0), psi.getCreationTime());
           
            psi.close();
            
            TestItem item = getItem("group1", "item1");
            assertNotNull(item);
            assertEquals(m_timer.getTimeMillis(1)-m_timer.getTimeMillis(0), item.getTotalTime());
            assertEquals(0L, item.getChildsTime());
        }
        int tID = m_timer.currentTimerId();
        {
            PSI psi1 = st.getStackItem("g1", "i1");
            PSI psi2 = st.getStackItem("g1", "i2");
            psi2.close();
            assertEquals(m_timer.getTimeMillis(tID+2)-m_timer.getTimeMillis(tID+1), 
                    psi1.getChildsTime());
            psi1.close();
            assertEquals(m_timer.getTimeMillis(tID+3)-m_timer.getTimeMillis(tID), 
                    getItem("g1","i1").getTotalTime());
            assertEquals(m_timer.getTimeMillis(tID+2)-m_timer.getTimeMillis(tID+1), 
                    getItem("g1","i1").getChildsTime());
        }
    }
    
    public void testRelease(){
        {
            try{
                st.release(new PSI(st, "g1", "i1", 823502L, 10));
            } catch(EmptyStackException e){}
        }
        {
            PSI psi0 = st.getStackItem("g2", "i0");
            PSI psi1 = st.getStackItem("g2", "i1");
            st.getStackItem("g2", "i2");
            PSI psi3 = st.getStackItem("g2", "i3");
            
        	Mockery ctx = new Mockery();
        	final Log log = ctx.mock(Log.class);
        	LogFactory lf = new ServerImplTest.TestLogFactory(log);
        	Api api = new Api();
        	api.setIntfImplementor(LogFactory.class, lf);
        	ApiStack.pushApi(api);
        	try{
        		psi3.close();
            	ctx.checking(new Expectations(){{
            		one(log).error(with(any(String.class)));
            	}});
	            psi1.close();	//must be error reported
	        	ctx.assertIsSatisfied();
	            psi0.close();	// no errors
        	} finally{
        		ApiStack.popApi();
        	}
        }
    }

    private TestItem getItem(String group, String name) {
        return search(new TestItem(group, name), new Comparator<TestItem>(){
            public int compare(TestItem arg0, TestItem arg1) {
                int v;
                return (v=arg0.getGroup().compareTo(arg1.getGroup())) == 0 ? 
                        v=arg0.getName().compareTo(arg1.getName()) :
                            v;
            }});
    }


    private TestItem search(TestItem srch, Comparator<TestItem> comp) {
        for (TestItem item : m_reported) {
            if(comp.compare(item, srch) == 0)
                return item; 
        }
        return null;
    }


    public String toString() {
        return null;
    }

    public void reportItem(String group, String name, long total, long childs, boolean bInner) {
        m_reported.add(new TestItem(group, name, total, childs));
    }

    public long get() {
        return m_timer.currentTimeMillis();
    }


	public void clearResult() {
		fail("not implemented");
	}


	public String getSnapshot() {
		// TODO Auto-generated method stub
		return null;
	}


	public void removeActivityStatus(Object activityKey) {
		// TODO Auto-generated method stub
		
	}


	public void updateActivityStatus(Object activityKey, ActivityStatus status) {
		// TODO Auto-generated method stub
		
	}

}
