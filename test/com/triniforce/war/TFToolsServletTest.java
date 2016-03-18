/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
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
import com.triniforce.extensions.PKPlugin;
import com.triniforce.soap.InterfaceDescription;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class TFToolsServletTest extends TFTestCase {
	
	
	public static class MyInitialContextFactory implements InitialContextFactory {

	    public Context getInitialContext(Hashtable<?, ?> arg0)
	            throws NamingException {

	        Context context = Mockito.mock(Context.class);
	        Mockito.when(context.lookup("java:/comp/env")).thenReturn(context);
	        Mockito.when(context.lookup("tftoolDb")).thenReturn(getDataSource());
	        return context;
	    }
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MyInitialContextFactory.class.getName());
		
	}
	
	static TestSvc p1 = Mockito.mock(TestSvc.class);
	
	public static class TestSvc{
		public void method_001(){
			p1.method_001();
		}

		public long method_002(){
			return ApiStack.getInterface(ITime.class).currentTimeMillis();
		}
		
	} 
	
	public static class TestEP extends PKPlugin{

		@Override
		public void doRegistration() {
			putExtension(UEPServiceEndoint.class, TestSvc.class);
		}

		@Override
		public void doExtensionPointsRegistration() {
		}
	}

	@Override
	public void test() throws Exception {
		TFToolsServlet srv = new TFToolsServlet();
		ServletConfig cfg = Mockito.mock(ServletConfig.class);
		Mockito.when(cfg.getInitParameter("plugins")).thenReturn(TestEP.class.getName());
		srv.init(cfg);
		
		assertNotNull(srv.getService());
		
		InterfaceDescription desc = srv.getInterfaceDescription();
		List<Operation> ops = desc.getOperations();
		assertEquals("war_method_001", ops.get(0).getName());
		
        assertEquals( "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":null}",
        		exec(srv, "{\"jsonrpc\":\"2.0\",\"method\":\"war_method_001\",\"params\":[],\"id\":1}"));
		
		Mockito.verify(p1).method_001();
		
		JSONParser p = new JSONParser();
		
		String resp = exec(srv, "{\"jsonrpc\":\"2.0\",\"method\":\"war_method_002\",\"params\":[],\"id\":1}");
		JSONObject obj = (JSONObject) p.parse(resp);
		new Date((Long) obj.get("result"));
	}
	
	public class StubServletOutputStream extends ServletOutputStream {
		public ByteArrayOutputStream baos = new ByteArrayOutputStream();

		public void write(int i) throws IOException {
			baos.write(i);
		}

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			// TODO Auto-generated method stub
			
		}
	}

	private String exec(TFToolsServlet srv, String str) throws IOException, ServletException {
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
