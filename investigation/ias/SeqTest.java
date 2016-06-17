/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;

import junit.framework.TestCase;
import junit.framework.TestResult;

import com.triniforce.db.test.TFTestCase;

public class SeqTest extends TFTestCase {
    
    TestCase m_test;
    
    HashSet<String> m_passed = new HashSet<String>();

    private File passedTests;

    private Class<?> m_testCls=null;
    private String m_testName;

    @Override
    protected void setUp() throws Exception {
        TFTestCase.bWriteToInitLogFile = false;
        super.setUp();
        passedTests = new File(TFTestCase.getTfTestFolder(), "passedtests.txt");
        if(passedTests.exists()){
            FileInputStream in = new FileInputStream(passedTests);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            while(r.ready()){
                String str = r.readLine();
                m_passed.add(str);
                if(str.contains(" "))
                    m_passed.add(str.split(" ")[0]);
            }
            in.close();
        }
        
        String testName = getTestProperty("testSeqTestCls");
        
        if(null != testName && !"".equals(testName)){
            m_testCls = Class.forName(testName);
            m_testName = getTestProperty("testSeqTestName", "test");
        }
    }
    
    @Override
    public void test() throws Exception {
        File f = getTestsLogFile();
        assertNotNull(f);
        FileInputStream testLog = new FileInputStream(f);
        FileOutputStream fOutPassed = new FileOutputStream(passedTests, true);
        BufferedReader r = new BufferedReader(new InputStreamReader(testLog));
        try{
            while(r.ready()){
                String str = r.readLine();
                if(str.startsWith("#") || m_passed.contains(str))
                    continue;
                trace("test: " + str);
                String[] strArr = str.split(" ");
                if(m_passed.contains(strArr[0]))
                    continue;
                Class<?> cls = Class.forName(strArr[0]);
                TestCase test = (TestCase) cls.newInstance();
                test.setName(strArr[1]);
                TestResult result = new TestResult();
                test.run(result);
                String status = "PASSED";
                if(result.failureCount() != 0){
                    status = "FAILED";
                    Enumeration fails = result.failures();
                    while(fails.hasMoreElements()){
                        trace(fails.nextElement());
                    }
                    if(null == m_testCls)
                        fail();
                }
                trace(status);

                if(null != m_testCls){
                    m_test = (TestCase) m_testCls.newInstance();
                    m_test.setName(m_testName);
                    result = new TestResult();
                    m_test.run(result);
                    if(result.failureCount() != 0){
                        Enumeration fails = result.failures();
                        while(fails.hasMoreElements()){
                            trace(fails.nextElement());
                        }
                        fail();
                    }
                }
                fOutPassed.write((strArr[0]+" " +strArr[1]+"\n").getBytes("utf-8"));
                fOutPassed.flush();    
            }
        } finally{
        	r.close();
            fOutPassed.close();           
            testLog.close();
        }
    }
}
