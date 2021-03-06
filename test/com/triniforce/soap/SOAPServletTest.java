/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.triniforce.db.test.TFTestCase;

public class SOAPServletTest extends TFTestCase {
	
	static ByteArrayOutputStream BYTE_OUT = null; 
	static ByteArrayInputStream BYTE_IN;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BYTE_OUT = new ByteArrayOutputStream();
	}
	
	static class TestOut extends ServletOutputStream{

		@Override
		public void write(int b) throws IOException {
			BYTE_OUT.write(b);
		}

		@Override
		public boolean isReady() {
			return false;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
		
		}
		
	}
	
	static class TestIn extends ServletInputStream{

		@Override
		public int read() throws IOException {
			return BYTE_IN.read();
		}

		@Override
		public boolean isFinished() {
			return false;
		}

		@Override
		public boolean isReady() {
			return false;
		}

		@Override
		public void setReadListener(ReadListener readListener) {

		}
		
	}
	
	static class TS extends SOAPServlet{
		private static final long serialVersionUID = -1216176304701711038L;
		public TS() {
			super("www.tst.com", "TS", TS.class.getPackage(), "");
		}
		
		public static class EP{
			public float method(int a, int b){ return ((float)a + (float)b) / 2.0f;}
			public float methodEx(int a, int b)throws Exception { throw new IOException();}
		}

		@Override
		public Object createService() {
			return new EP();
		}

		@Override
		protected InterfaceDescription generateInterfaceDescription(
				InterfaceDescription oldDesc) {
			return m_gen.parse(null, EP.class);
		}
		
	}
	
	TS srv = new TS();
	
	public void testJSONRequest() throws ServletException, IOException, TransformerException{
		
		Mockery ctx = new Mockery();
		final ServletConfig cfg = ctx.mock(ServletConfig.class);

		ctx.checking(new Expectations(){{
		}});
		
		srv.init(cfg);
		
		final HttpServletRequest req = ctx.mock(HttpServletRequest.class);
		final HttpServletResponse res = ctx.mock(HttpServletResponse.class);
		
		setSoapReq("{\"jsonrpc\":\"2.0\",\"method\":\"method\",\"params\":[3,6],\"id\":1}");

		
		ctx.checking(new Expectations(){{
			one(req).getInputStream(); will(returnValue(new TestIn()));
			one(req).getContentType(); will(returnValue("text/json"));
			
			allowing(res).getOutputStream(); will(returnValue(new TestOut()));

			one(res).setContentType(with(any(String.class)));
			one(res).setContentLength(with(any(int.class)));
			one(res).setStatus(HttpServletResponse.SC_OK);
			one(res).flushBuffer();
			
		}});
		srv.doServiceCall(req, res);
		System.out.write(BYTE_OUT.toByteArray());
		
		assertEquals("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":4.5}", new String (BYTE_OUT.toByteArray()));
		
		{
			BYTE_OUT = new ByteArrayOutputStream();
			setSoapReq("{\"jsonrpc\":\"2.0\",\"method\":\"method\",\"params\":[3,6],\"id\":1}");
			ctx.checking(new Expectations(){{
				one(req).getInputStream(); will(returnValue(new TestIn()));
				one(req).getContentType(); will(returnValue("application/json"));
				
				allowing(res).getOutputStream(); will(returnValue(new TestOut()));

				one(res).setContentType(with(any(String.class)));
				one(res).setContentLength(with(any(int.class)));
				one(res).setStatus(HttpServletResponse.SC_OK);
				one(res).flushBuffer();
				
			}});
			srv.doServiceCall(req, res);
			assertEquals("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":4.5}", new String (BYTE_OUT.toByteArray()));
		}
		
	}
	
	public void testJSONRequestEx() throws ServletException, IOException, TransformerException, ParseException{
		
		Mockery ctx = new Mockery();
		final ServletConfig cfg = ctx.mock(ServletConfig.class);

		ctx.checking(new Expectations(){{
		}});
		
		srv.init(cfg);
		
		final HttpServletRequest req = ctx.mock(HttpServletRequest.class);
		final HttpServletResponse res = ctx.mock(HttpServletResponse.class);
		
		setSoapReq("{\"jsonrpc\":\"2.0\",\"method\":\"methodEx\",\"params\":[3,6],\"id\":1}");

		
		ctx.checking(new Expectations(){{
			one(req).getInputStream(); will(returnValue(new TestIn()));
			one(req).getContentType(); will(returnValue("text/json"));
			
			allowing(res).getOutputStream(); will(returnValue(new TestOut()));

			one(res).setContentType(with(any(String.class)));
			one(res).setContentLength(with(any(int.class)));
			one(res).setStatus(500);
			one(res).flushBuffer();
			
		}});
		srv.doServiceCall(req, res);
		System.out.write(BYTE_OUT.toByteArray());
		
		JSONParser p = new JSONParser();
		JSONObject json1 = (JSONObject) p.parse(new String (BYTE_OUT.toByteArray()));
		trace(json1);
		assertEquals("2.0", json1.get("jsonrpc"));
		assertEquals(1L, json1.get("id"));
		JSONObject err = (JSONObject) json1.get("error");
		assertEquals("java.io.IOException: null", err.get("message"));
		assertEquals(500L, err.get("code"));
		assertEquals("", err.get("stackTrace"));
		
	}
	
	private void setSoapReq(String method) throws TransformerException, IOException {
		BYTE_IN = new ByteArrayInputStream(method.getBytes("utf-8"));
	}
}
