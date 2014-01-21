/* 
 * Copyright(C) UnTill 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.w3c.dom.Document;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescription;
import com.triniforce.soap.InterfaceDescriptionGenerator;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.SOAPServlet;
import com.triniforce.utils.ApiAlgs;

public abstract class SoapServerTestCase extends TFTestCase {
	
	private URL url;
	private SOAPServlet servlet;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		servlet = startWebServer();
		setUrl(new URL("http://localhost:8888/wmapi"));
	}
	

	@Override
	protected void tearDown() throws Exception {
		stopWebServer();
		super.tearDown();
	}
	
	protected abstract SOAPServlet startWebServer() throws Exception;
	protected abstract void stopWebServer() throws Exception;

	
	protected void setUrl(URL url){
		this.url = url; 
	}
	
	public URL getUrl(){
		return url;
	}
	
	public SOAPServlet getServlet(){
		return servlet;
	}
	
	protected Object exec(String fun, Object... args) throws Exception {
		
		SOAPDocument soap = new InterfaceDescriptionGenerator.SOAPDocument();
		soap.m_bIn = true;
		soap.m_method = fun;
		soap.m_args = args;
		soap.m_soap = InterfaceDescriptionGenerator.soapenv;
		
		InterfaceDescription desc = servlet.getInterfaceDescription();
		InterfaceDescriptionGenerator gen = servlet.getInterfaceGenerator();
		Document doc = gen.serialize(desc, soap);
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		gen.writeDocument(buf, doc);
		
		String resp = exec(url, fun, new String(buf.toByteArray()));
		
		SOAPDocument soapResp = gen.deserialize(desc, new ByteArrayInputStream(resp.getBytes()));
		return soapResp.m_args.length > 0 ? soapResp.m_args[0] : null;
		
	}
	
	private String exec(URL url, String msg, String req) throws Exception  {
		ApiAlgs.getLog(this).trace(msg);
		ApiAlgs.getLog(this).trace(req);
		
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "text/xml");
		
		OutputStream output = con.getOutputStream();

		output.write(req.getBytes("utf-8"));
		output.flush();
		output.close();
		InputStream input = con.getInputStream();

		StringBuffer strBuf = new StringBuffer();
		BufferedReader r = new BufferedReader(new InputStreamReader(input));
		while(r.ready()){
			strBuf.append(r.readLine());
			strBuf.append('\n');
		}
		ApiAlgs.getLog(this).trace(strBuf.toString());
		return strBuf.toString();
	}	


	
	
}
