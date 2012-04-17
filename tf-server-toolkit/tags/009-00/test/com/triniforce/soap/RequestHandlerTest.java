/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.ApiAlgs;

public class RequestHandlerTest extends TFTestCase {
    
	@SoapInclude(extraClasses={TestService.C2.class})
    public static class TestService{
        public int method1(String arg0){
           return 2008; 
        }
        
        public static class C1{}
        public static class C2 extends C1{}
        
        public int method2(C1 v){
			return 0;
        }
        
        public enum ENM1{V1, V2};
        
        public ENM1 method3(ENM1 in){
        	ApiAlgs.getLog(this).trace(in);
			return ENM1.V1;
        }
        
        public Object method4(){
        	return "String_006334".getBytes();
        }
    }

    @Override
    public void test() throws Exception {
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, TestService.class);
        RequestHandler handler = new RequestHandler(gen, desc, new RequestHandler.ReflectServiceInvoker(new TestService()));
        
        String REQ1 = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "  <soap:Body>"+
            "    <method1 xmlns=\"http://tempuri.org/\">"+
            "      <arg0>test_string</arg0>"+
            "    </method1>"+
            "  </soap:Body>"+
            "</soap:Envelope>";
        
        handler.exec(new ByteArrayInputStream(REQ1.getBytes("utf-8")), System.out);
        
        handler.exec(null, System.out);
        

        String REQ2 = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "  <soap:Body>"+
            "    <rsg:method2 xmlns:rsg=\"http://tempuri.org/\">"+
            "      <rsg:arg0 xsi:type=\"rsg:C2\"></rsg:arg0>"+
            "    </rsg:method2>"+
            "  </soap:Body>"+
            "</soap:Envelope>";
        
        handler.exec(new ByteArrayInputStream(REQ2.getBytes("utf-8")), System.out);
        
        gen.writeDocument(System.out, gen.generateWSDL(desc.getWsdlDescription(), "test.test"));
        
        String REQ3 = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "  <soap:Body>"+
            "    <rsg:method3 xmlns:rsg=\"http://tempuri.org/\">" +
            "		<rsg:arg0>V3</rsg:arg0>"+
            "    </rsg:method3>"+
            "  </soap:Body>"+
            "</soap:Envelope>";
        
        handler.exec(new ByteArrayInputStream(REQ3.getBytes("utf-8")), System.out);
        
        String REQ4 = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "  <soap:Body>"+
            "    <rsg:method4 xmlns:rsg=\"http://tempuri.org/\">" +
            "    </rsg:method4>"+
            "  </soap:Body>"+
            "</soap:Envelope>";
        
        handler.exec(new ByteArrayInputStream(REQ4.getBytes("utf-8")), System.out);
    }
    
    static boolean bShutDown = false;
    
    static class TestThread extends Thread{

        private InterfaceDescriptionGenerator m_gen;
        private InterfaceDescription m_desc;
        private TestService m_svc;
        private int m_count;

        public TestThread(InterfaceDescriptionGenerator gen, InterfaceDescription desc, TestService service) {
            m_gen = gen;
            m_desc = desc;
            m_svc = service;
            m_count = 0;
        }
        
        @Override
        public void run() {
            try {
                while(!bShutDown){
                    RequestHandler h = new RequestHandler(m_gen, m_desc, new RequestHandler.ReflectServiceInvoker(m_svc));
                    String REQ1 = 
                        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
                        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                        "  <soap:Body>"+
                        "    <method1 xmlns=\"http://tempuri.org/\">"+
                        "      <arg0>test_string</arg0>"+
                        "    </method1>"+
                        "  </soap:Body>"+
                        "</soap:Envelope>";
                    
                    ByteArrayInputStream in;
                    in = new ByteArrayInputStream(REQ1.getBytes("utf-8"));
                    h.exec(in, System.out);
                    in.close();
                    m_count ++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    } 
    
    public void testStress() throws InterruptedException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, TestService.class);
        TestService service = new TestService();
        
        ArrayList<TestThread> threads = new ArrayList<TestThread>();
        for(int i=0; i<10; i++){
            threads.add(new TestThread(gen, desc, service));
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        Thread.sleep(1000);
        
        bShutDown = true;
        
        for (TestThread thread : threads) {
            thread.join();
            ApiAlgs.getLog(this).trace("allRequests = "+thread.m_count); 
        }
        
    }
}
