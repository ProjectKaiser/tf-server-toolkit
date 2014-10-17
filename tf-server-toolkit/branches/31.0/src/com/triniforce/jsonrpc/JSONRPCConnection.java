/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.jsonrpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

//import org.apache.http.HttpEntity;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;

import com.triniforce.utils.ApiAlgs;

public class JSONRPCConnection {

	private String m_url;
	private int m_connectTimeout;
	private int m_readTimeout;

	public JSONRPCConnection(String url, int connectTimeout, int readTimeout) {
		this.m_url = url;
		this.m_connectTimeout = connectTimeout;
		this.m_readTimeout = readTimeout;
	}

	public String invoke(String req) {
		//ApiAlgs.getLog(this).trace(req);
		String result;
		try {
			URL url = new URL(m_url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(m_connectTimeout);
			connection.setReadTimeout(m_readTimeout);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/json");

			OutputStream output = connection.getOutputStream();
			try {
				output.write(req.getBytes("UTF-8"));
				output.flush();
			} finally {
				try { output.close(); } catch (Throwable e) { }
			}

			InputStream input = connection.getInputStream();
			try {
				result = new String(readAllBytesFromInputStream(input, connection.getContentLength()), "UTF-8");
			} finally {
				try { input.close(); } catch (Throwable e) { }
			}
		} catch (IOException e) {
			//ApiAlgs.rethrowException(e);
			throw new ApiAlgs.RethrownException(e);
		}
		//ApiAlgs.getLog(this).trace(result);
		return result;
	}

//	public String invoke2(String req) {
//		//ApiAlgs.getLog(this).trace(req);
//		String result;
//		try {
//			CloseableHttpClient httpclient = HttpClients.createDefault();
//			RequestConfig requestConfig = RequestConfig.custom()
//			        .setSocketTimeout(m_readTimeout)
//			        .setConnectTimeout(m_connectTimeout)
//			        .build();
//
//			StringEntity entity = new StringEntity(req, ContentType.create("text/json"));
//
//			HttpPost httpPost = new HttpPost(m_url);
//			httpPost.setConfig(requestConfig);
//			httpPost.setEntity(entity);
//			CloseableHttpResponse response = httpclient.execute(httpPost);
//			try {
//				HttpEntity responseEntity = response.getEntity();
//	
//				InputStream input = responseEntity.getContent();
//				try {
//					result = new String(readAllBytesFromInputStream(input, (int) responseEntity.getContentLength()), "UTF-8");
//				} finally {
//					try { input.close(); } catch (Throwable e) { }
//				}
//	
//			} finally {
//				response.close();
//			}
//		} catch (Exception e) {
//			//ApiAlgs.rethrowException(e);
//			throw new ApiAlgs.RethrownException(e);
//		}
//		//ApiAlgs.getLog(this).trace(result);
//		return result;
//	}

	public static byte[] readAllBytesFromInputStream(InputStream input, int contentLength) throws IOException {
		byte[] bytes;
		if (contentLength < 0) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			bytes = new byte[0x1000];
			int count;
			while ((count = input.read(bytes, 0, bytes.length)) != -1) {
				buffer.write(bytes, 0, count);
			}
			buffer.flush();
			bytes = buffer.toByteArray();
		} else {
			bytes = new byte[contentLength];
			int n = 0;
			while (n < contentLength) {
			    int count = input.read(bytes, 0 + n, contentLength - n);
			    if (count < 0) {
			    	bytes = Arrays.copyOf(bytes, n);
			    	break;
			    }
			    n += count;
			}
		}
		return bytes;
	}
}
