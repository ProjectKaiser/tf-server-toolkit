/* 
 * Copyright(C) UnTill 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import java.net.URL;

import com.triniforce.soap.InterfaceDescription;
import com.triniforce.soap.InterfaceDescriptionGenerator;
import com.triniforce.soap.SOAPServlet;

public abstract class SoapServerTestCase extends SoapTestCase {
	
	private SOAPServlet servlet;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		servlet = startWebServer();
		setUrl(new URL("http://localhost:8888/wmapi"));
		
		InterfaceDescription desc = servlet.getInterfaceDescription();
		InterfaceDescriptionGenerator gen = servlet.getInterfaceGenerator();
		setDescription(gen, desc);
	}
	

	@Override
	protected void tearDown() throws Exception {
		stopWebServer();
		super.tearDown();
	}
	
	protected abstract SOAPServlet startWebServer() throws Exception;
	protected abstract void stopWebServer() throws Exception;
	
	public SOAPServlet getServlet(){
		return servlet;
	}
	
	
}
