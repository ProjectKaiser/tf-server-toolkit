/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.jsonrpc;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.triniforce.utils.ApiAlgs;

public class JSONRPCConnection {

	private static CloseableHttpClient m_httpclient = null;

	private static CloseableHttpClient getHttpClient() {
		if (m_httpclient == null)
			m_httpclient = HttpClients.createDefault();
		return m_httpclient;
	}

	private String m_url;
	private int m_connectTimeout;
	private int m_readTimeout;

	public JSONRPCConnection(String url, int connectTimeout, int readTimeout) {
		this.m_url = url;
		this.m_connectTimeout = connectTimeout;
		this.m_readTimeout = readTimeout;
	}

	public String invoke(String req) {
		try {
			String result;

			RequestConfig requestConfig = RequestConfig.custom()
			        .setSocketTimeout(m_readTimeout)
			        .setConnectTimeout(m_connectTimeout)
			        .build();

			StringEntity entity = new StringEntity(req, ContentType.create("text/json"));

			HttpPost httpPost = new HttpPost(m_url);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(entity);

			CloseableHttpResponse response = getHttpClient().execute(httpPost);
			try {
				HttpEntity responseEntity = response.getEntity();
				result = EntityUtils.toString(responseEntity, "UTF-8");
			} finally {
				response.close();
			}

			return result;
		} catch (Exception e) {
			throw new ApiAlgs.RethrownException(e);
		}
	}

}
