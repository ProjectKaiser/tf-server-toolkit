/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.w3c.dom.Document;

import com.triniforce.soap.InterfaceDescription;
import com.triniforce.soap.InterfaceDescriptionGenerator;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.utils.ApiAlgs;

public class BasicServerClient implements InvocationHandler{

	private URL m_url;
	private InterfaceDescription m_desc;
	private InterfaceDescriptionGenerator m_gen;
	private String m_prefix;


	public BasicServerClient(URL url, Class svc) throws IntrospectionException {
		m_url = url;
		m_gen = new InterfaceDescriptionGenerator("http://soap.tftool.untill.eu/", "tftool");
		m_desc = m_gen.parse(null, Arrays.asList(svc));
		String pkg = svc.getPackage().getName();
		m_prefix = pkg.substring(pkg.lastIndexOf('.')+1);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
			SOAPDocument soap = new InterfaceDescriptionGenerator.SOAPDocument();
			soap.m_bIn = true;
			soap.m_method = m_prefix + "_" + method.getName();
			soap.m_args = args;
			soap.m_soap = InterfaceDescriptionGenerator.soapenv;
			
			Document doc = m_gen.serialize(m_desc, soap);
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			m_gen.writeDocument(buf, doc);
			
			String resp = exec(m_url, soap.m_method, new String(buf.toByteArray()));
			
			SOAPDocument soapResp = m_gen.deserialize(m_desc, new ByteArrayInputStream(resp.getBytes()));
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
