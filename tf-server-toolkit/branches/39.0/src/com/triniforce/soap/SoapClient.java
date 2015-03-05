/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.w3c.dom.Document;

import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.utils.ApiAlgs;

public class SoapClient {

	private URL m_url;
	private InterfaceDescription m_desc;
	private InterfaceDescriptionGenerator m_gen;

	public SoapClient(URL url, InterfaceDescription desc, InterfaceDescriptionGenerator gen) {
		m_url = url;
		m_desc = desc;
		m_gen = gen;
	}
	
	public Object exec(String fun, Object... args) throws Exception {
		
		SOAPDocument soap = new InterfaceDescriptionGenerator.SOAPDocument();
		soap.m_bIn = true;
		soap.m_method = fun;
		soap.m_args = args;
		soap.m_soap = InterfaceDescriptionGenerator.soapenv;
		
		Document doc = m_gen.serialize(m_desc, soap);
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		m_gen.writeDocument(buf, doc);
		
		String resp = exec(m_url, fun, new String(buf.toByteArray()));
		
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
