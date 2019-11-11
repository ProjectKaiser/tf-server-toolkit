/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;

import java.util.ArrayList;
import java.util.Random;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.plugins.kernel.DbQueue.DbRecord;
import com.triniforce.server.plugins.kernel.tables.TDbQueues;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;
import com.triniforce.utils.IProfiler;
import com.triniforce.utils.IProfilerStack.PSI;

public class DbQueueTest extends BasicServerTestCase {


    private static final long SNAKE_TEST_TIME = 10000L;
    private static final int READER_NUM =5;
    
    protected void setUp() throws Exception {
        super.setUp();
        m_server.enterMode(Mode.Running);
        ISrvSmartTran tr = ApiStack.getInterface(ISrvSmartTran.class);
        tr.delete(TDbQueues.class, new IName[]{}, new Object[]{});
        ISrvSmartTranFactory.Helper.commitAndStartTran();
    }

    protected void tearDown() throws Exception {
        m_server.leaveMode();
        super.tearDown();
    }

    public void testIFileQueueFactory(){
        IDbQueueFactory fqf = ApiStack.getApi().getIntfImplementor(IDbQueueFactory.class);
        
        {
            //Different queueIds
            IDbQueue fq = fqf.getDbQueue(0L);
            assertNotNull(fq);
            fq.put("D1");
            fq.put("D2");
            
            IDbQueue fq2 = fqf.getDbQueue(0L);
            fq2.put("D4");
            fq2.put("D5");

        }
        {
            IDbQueue q1 = fqf.getDbQueue(0L);
            IDbQueue q2 = fqf.getDbQueue(0L);
            assertSame(q1, q2);

            IDbQueue q3 = fqf.getDbQueue(6834);
            assertNotSame(q1, q3);
            
        }
    }
    
    static int DELAY_ERR = 150; 
    
    //@Override
    public void test() throws Exception {
        IDbQueueFactory fqf = ApiStack.getApi().getIntfImplementor(IDbQueueFactory.class);
        
        IDbQueue fq = fqf.getDbQueue(10L);
        
        {//test put
            clearQueue(fq);
            
            fq.put("Data1");
            fq.put("Data2");
            fq.put("Data3");
            fq.put("Data4");
            
            assertEquals("Data1", fq.get(10));
            assertEquals("Data2", fq.get(10));
        }
        {//test peek
            clearQueue(fq);
            
            fq.put("Peek_test1");
            fq.put("Peek_test2");
            assertEquals("Peek_test1", fq.peek(10));
            assertEquals("Peek_test1", fq.peek(10));
            fq.get(0);
            fq.get(0);
            assertNull(fq.peek(0));
            
            long time = System.currentTimeMillis();
            long timeout = 1000L;
            fq.peek(timeout);
            long delta = System.currentTimeMillis()-time;
            assertTrue(""+delta, delta >= timeout-DELAY_ERR);
            assertTrue(""+delta, delta < timeout+DELAY_ERR);

            timeout = 100L;
            time = System.currentTimeMillis();
            assertEquals(null, fq.peek(timeout));
            delta = System.currentTimeMillis()-time;
            assertTrue(""+delta, delta >= timeout-DELAY_ERR);
            assertTrue(""+delta, delta < timeout+DELAY_ERR);
            
            fq.put("Peek_test3");
            time = System.currentTimeMillis();
            timeout = 300L;
            assertEquals("Peek_test3", fq.peek(timeout));
            delta = System.currentTimeMillis()-time;
            assertTrue(""+delta, delta < timeout+DELAY_ERR);
            
        }
        {//test get
            clearQueue(fq);
            
            fq.put("get_test1");
            fq.put("get_test2");
            assertEquals("get_test1", fq.get(10));
            assertEquals("get_test2", fq.get(10));
            assertNull(fq.get(10));
            
            long time = System.currentTimeMillis();
            long timeout = 500L;
            assertNull(fq.peek(timeout));
            long delta = System.currentTimeMillis()-time;
            assertTrue(""+delta, delta >= timeout-DELAY_ERR);
            assertTrue(""+delta, delta < timeout+DELAY_ERR);
        }
        
        {//timeoutMilliseconds == 0
            clearQueue(fq);
            long time = System.currentTimeMillis();
            assertNull(fq.get(0L));
            long delta = System.currentTimeMillis()-time;
            assertTrue(Long.toString(delta), delta < 100);
        }
        
        {
        	assertEquals(777L, fqf.getDbQueue(777L).getId());
        	assertEquals(766L, fqf.getDbQueue(766L).getId());
        }
    }    
   
    public void testTransaction(){
        IDbQueue fq = getFileQueue();
        clearQueue(fq);
        Mockery context = new Mockery();
        final ISrvSmartTranFactory trnFact = context.mock(ISrvSmartTranFactory.class);
        Api api = new Api();
        api.setIntfImplementor(ISrvSmartTranFactory.class, trnFact);
        ApiStack.pushApi(api);
        try{
            context.checking(new Expectations(){{
                one(trnFact).pop();
                one(trnFact).push();
            }});
            fq.get(1000L);
            context.assertIsSatisfied();
        } finally{
            ApiStack.popApi();
        }
    }
    
    public void testNotify() throws InterruptedException{
        m_server.enterMode(Mode.Running);
        try{
            clearQueue(getFileQueue());
            ISrvSmartTranFactory.Helper.commit();
        } finally{
            m_server.leaveMode();
        }
        
        Thread producer = new Thread(){
            @Override
            public void run() {
                m_server.enterMode(Mode.Running);
                try{
                    sleep(1000);    //Small delay for wait
                    IDbQueue fq2 = getFileQueue(); 
                    fq2.put("Producer thread message");
                    ISrvSmartTranFactory.Helper.commit();  //<- notify here
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }finally{
                    m_server.leaveMode();
                }
            }
        };
        
        IDbQueue fq = getFileQueue();
        producer.start();           
        long time = System.currentTimeMillis();
        int timeout = 5000;
        Object recieved = fq.get(timeout);
        long delta = System.currentTimeMillis()-time;
        assertEquals("Producer thread message", recieved);
        assertTrue(""+delta, delta < timeout-DELAY_ERR);
        ISrvSmartTranFactory.Helper.commit();
        producer.join();
    }

    private void clearQueue(IDbQueue fq) {
        fq.clean();
    }
    
    private class TestReaderThread extends Thread{
        BasicServer m_srv;
        private String m_terminateMesage;
        private int m_errors;
        private int m_correctNum;
        public TestReaderThread(String threadName, BasicServer srv, String terminateMsg) {
            m_srv = srv;
            m_terminateMesage = terminateMsg;
            setName(threadName);
            m_correctNum = 0;
            m_errors = 0;
            start();
        }
        
        @Override
        public void run() {
            String msg;
            do{
                m_srv.enterMode(Mode.Running);
                try{
                    msg = (String) getFileQueue().get(1000);
                    if(null != msg){
                        if(msg.equals("Commit")){
                            m_correctNum ++;
                        }
                        else if(m_terminateMesage.equals(msg)){
                            
                        }else{
                            m_errors ++;
                        }
                    }
                    ISrvSmartTranFactory.Helper.commit();
                }finally{
                    m_srv.leaveMode();
                }
            }while(!m_terminateMesage.equals(msg));
            
            trace("Thread \""+getName()+"\" finished.");
        }   
    }
    
    private class TestWriterThread extends Thread{
        BasicServer m_srv;
        private long m_finishTime;
        private int m_messageNum;
        private int m_commitNum;
        public TestWriterThread(String threadName, BasicServer srv, long finishTime) {
            m_srv = srv;
            m_finishTime = finishTime;
            setName(threadName);
            m_messageNum = 0;
            m_commitNum = 0;
            start();
        }
        @Override
        public void run() {
            Random random = new Random();
            while(System.currentTimeMillis() < m_finishTime){
                m_srv.enterMode(Mode.Running);
                try{
                    boolean bCommit = random.nextDouble() < 0.5;
                    String message = bCommit ? "Commit" : "Rollback";
                    getFileQueue().put(message);
                    sleep(100);
                    m_messageNum++;
                    if(bCommit){
                        m_commitNum++;
                        ISrvSmartTranFactory.Helper.commit();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally{
                    m_srv.leaveMode();
                }
            }
            
            trace("Thread \""+getName()+"\" finished.");
        }   
    }
    
    static IDbQueue getFileQueue(){
        IDbQueueFactory fqf = ApiStack.getApi().getIntfImplementor(IDbQueueFactory.class);
        return fqf.getDbQueue(0L);
    }
    
    public void testShake() throws InterruptedException{
        IProfiler pr = ApiStack.getApi().getIntfImplementor(IProfiler.class);
        pr.clearResult();
        
        trace("===Shake test started===");
        
        String terminateMesage = "stop";
        
        //prepare test
        m_server.enterMode(Mode.Running);
        try{
            clearQueue(getFileQueue());
            ISrvSmartTranFactory.Helper.commit();
        } finally{
            m_server.leaveMode();
        }
        
        ArrayList<TestWriterThread> threads = new ArrayList<TestWriterThread>();
        long finishTime = System.currentTimeMillis() + SNAKE_TEST_TIME;
        for (int i=0; i<READER_NUM; i++){
            threads.add(new TestWriterThread("Writer No"+i, getServer(), finishTime));
        }
        
        TestReaderThread reader = new TestReaderThread("Reader", getServer(), terminateMesage);
        
        int commitsNum=0, msgNum=0;
        
        for (TestWriterThread thread : threads) {
            thread.join();
            commitsNum += thread.m_commitNum;
            msgNum += thread.m_messageNum;
        }
        
        trace("Writers make "+commitsNum+"("+msgNum+")"+" messages");
        trace("On this moment reader gets "+reader.m_correctNum+" messages");
        
        
        getFileQueue().put(terminateMesage);
        ISrvSmartTranFactory.Helper.commit();
        
        reader.join();
        trace(pr.toString());
        
        assertEquals(commitsNum, reader.m_correctNum);
        assertEquals(0, reader.m_errors);
        
        trace("Shake test completed.");
        
    }
    
    static final int MSG_NUM = 400;
    
    Boolean readyFlag = false;
    final Object syncObject = new Object();
    int recievedData=0;
    boolean dataLost=true;
        
    public void testNoDataLost() throws InterruptedException{
        trace("===No data lost test===");
        Thread writer = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(500);
                    for(int i=0; i<MSG_NUM; i++){
                        synchronized(syncObject){
                            readyFlag = false;
                            m_server.enterMode(Mode.Running);
                            try{
                                if((i+1) % 50 == 0)
                                    trace("Msg: "+(i+1));
                                getFileQueue().put("Data");
                                ISrvSmartTranFactory.Helper.commit();
                            }finally{
                                m_server.leaveMode();
                            }
                            syncObject.wait(5000);
                            if(!readyFlag){
                                dataLost = true;
                                fail();
                            }
                            assertTrue(readyFlag);
                        }
                    }
                    dataLost = false;
                    trace("Thread \"writer\" finished.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread reader = new Thread(){
            @Override
            public void run() {
                boolean bShutDown = false;
                
                while(!bShutDown){
                    m_server.enterMode(Mode.Running);
                    try{
                        String data;
                        data = (String) getFileQueue().get(100000);
                        if("stop".equals(data))
                            bShutDown = true;
                        if("Data".equals(data)){
                            recievedData++;
                        }
                        
                        ISrvSmartTranFactory.Helper.commit();
                        
                    }finally{
                        m_server.leaveMode();
                    }
                        
                    synchronized(syncObject){
                        readyFlag = true;
                        syncObject.notify();
                    }
                }
                
                trace("Thread \"reader\" finished.");
            }
        };
        
        PSI psi = ApiAlgs.getProfItem("Test", "full time");
        
        writer.start();
        reader.start();
        
        writer.join();
        getFileQueue().put("stop"); //stop reader thread
        ISrvSmartTranFactory.Helper.commit();
        reader.join();
        
        ApiAlgs.closeProfItem(psi);
        
        IProfiler pr = ApiStack.getApi().getIntfImplementor(IProfiler.class);
        trace(pr.toString());
        
        assertFalse(dataLost);
        assertEquals(MSG_NUM, recievedData);
        
    }
    
    public void testBug1(){
    	if(ApiStack.getInterface(IDatabaseInfo.class).getDbType().equals(DbType.H2))
    		return ;
        {
            IDbQueueFactory.Helper.cleanQueue(10002);
            IDbQueue q1 = IDbQueueFactory.Helper.getQueue(10002);
            q1.put("d1");
            q1.put("d2");
            q1.put("d3");
            q1.put("d4");
            IDbQueueFactory.Helper.cleanQueue(10003);
            q1 = IDbQueueFactory.Helper.getQueue(10003);
            q1.put("d1");
            q1.put("d2");
            q1.put("d3");
            q1.put("d4");
            ISrvSmartTranFactory.Helper.commit();
        }
        ISrvSmartTranFactory.Helper.push();
        try{
            IDbQueue q1 = IDbQueueFactory.Helper.getQueue(10002);
            q1.get(0L);
            q1.put("fsd");
    
            ISrvSmartTranFactory.Helper.push();
            try{
                IDbQueue q2 = IDbQueueFactory.Helper.getQueue(10002);
                q2.put("fsd");
            }finally{
                ISrvSmartTranFactory.Helper.pop();
            }    
        }finally{
            ISrvSmartTranFactory.Helper.pop();
        }
    }
    
    public void testClean(){
        IDbQueue q1 = IDbQueueFactory.Helper.getQueue(10002);
        q1.put("d1");
        q1.put("d2");
        q1.put("d3");
        q1.put("d4");
        
        q1.clean();
        
        assertNull(q1.get(0L));

    }
    
    public void testPut(){
    	IDbQueueFactory.Helper.cleanQueue(10004);
    	IIdGenerator idGen = ApiStack.getInterface(IIdGenerator.class);
    	long stdId = idGen.getKey();
    	DbQueue q1 = (DbQueue) IDbQueueFactory.Helper.getQueue(10004);
    	q1.put("data_001");
    	q1.put("data_002");
    	DbRecord req1 = q1.peekInternal(0L);
    	q1.get(0);
    	DbRecord req2 = q1.peekInternal(0L);
    	q1.get(0);
    	
    	assertEquals(req1.recId+1, req2.recId);
    	assertEquals(stdId+1, idGen.getKey());
    	assertTrue(""+req1.recId, req1.recId > 10000);
//    	assertTrue(""+req1.recId, stdId < req1.recId);
    	
    	
    }
}
