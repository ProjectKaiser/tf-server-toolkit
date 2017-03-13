/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescription;
import com.triniforce.utils.TFUtils;

public class BasicServerServletTest extends TFTestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		InitialContext ic = new InitialContext();
		
        ic.createSubcontext("java:/comp/env");  
		ic.bind("java:/comp/env/BasicServerDb", getDataSource());
		
		File tempF = getTempTestFolder();
		File plf = new File(tempF, "plugins");
		plf.mkdir();
		copyTestResources(new String[]{"test_pligin.jar"}, plf);
		ic.bind("java:/comp/env/BasicServerHome", tempF.getAbsolutePath());
		
		TFUtils.copyStream(new ByteArrayInputStream("admin_password=".getBytes()), new FileOutputStream(new File(tempF, "config.properties")));
	}

	@Override
	public void test() throws Exception {
		BasicServerServlet srv = new BasicServerServlet();
		ServletConfig cfg = Mockito.mock(ServletConfig.class);
		srv.init(cfg);
		try{
			assertNotNull(srv.getService());
			
			InterfaceDescription desc = srv.getInterfaceDescription();
			assertNotNull(desc.getOperation("testpkg_method_001"));
	//		assertEquals("testpkg_method_001", ops.get(0).getName());
			
	        assertEquals( "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":null}",
	        		exec(srv, "{\"jsonrpc\":\"2.0\",\"method\":\"testpkg_method_001\",\"params\":[],\"id\":1}"));
			
	//		Mockito.verify(TestEP.p1).method_001();
			
			JSONParser p = new JSONParser();
			
			String resp = exec(srv, "{\"jsonrpc\":\"2.0\",\"method\":\"testpkg_method_002\",\"params\":[],\"id\":1}");
			JSONObject obj = (JSONObject) p.parse(resp);
			new Date((Long) obj.get("result"));
		}
		finally{
			srv.destroy();
		}
		
		{
			
			InitialContext ic = new InitialContext();
			Context sc = (Context) ic.lookup("java:/comp/env");  
			sc.unbind("tftoolDb");
			BasicServerServlet srv2 = new BasicServerServlet();
			srv2.init(cfg);
			srv2.destroy();
			
		}
	}
	
	public class StubServletOutputStream extends ServletOutputStream {
		public ByteArrayOutputStream baos = new ByteArrayOutputStream();

		public void write(int i) throws IOException {
			baos.write(i);
		}

		@Override
		public boolean isReady() {
			return false;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			
		}
	}

	private String exec(BasicServerServlet srv, String str) throws IOException, ServletException {
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);       
        HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
        Mockito.when(req.getMethod()).thenReturn("POST");
        Mockito.when(req.getContentType()).thenReturn("text/json");
        ServletInputStream in = Mockito.mock(ServletInputStream.class);
        byte[] myBinaryData = str.getBytes();
        
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(myBinaryData);

        Mockito.when(in.read(Matchers.<byte[]>any())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                byte[] output = (byte[]) args[0];
                return byteArrayInputStream.read(output);

            }
        });

        StubServletOutputStream out = new StubServletOutputStream();
        Mockito.when(req.getInputStream()).thenReturn(in);
        Mockito.when(res.getOutputStream()).thenReturn(out);
		srv.service(req, res);
		
		return out.baos.toString();
	}
}
