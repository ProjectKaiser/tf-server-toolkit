/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.soap.RequestHandler.IServiceInvoker;

public class MultiClassServiceInvoker implements IServiceInvoker {
	
	static final char DELIM = '_';
	
	public static class EServiceEndpointAlreadyRegistered extends RuntimeException{
		private static final long serialVersionUID = 2694989672149506449L;
		public EServiceEndpointAlreadyRegistered(String prefix) {
			super(prefix);
		}
	}
	
	public static class EServiceEnpointNotFound extends RuntimeException{
		private static final long serialVersionUID = 2694989672149506449L;
		public EServiceEnpointNotFound(String prefix) {
			super(prefix);
		}
	}
	
	Map<String, IServiceInvoker> m_endpoints = new HashMap<String, IServiceInvoker>();
	
	public void addServiceEndpoint(String prefix, Object serviceEndpoint){
		if(prefix.indexOf(DELIM)>=0)
			throw new IllegalArgumentException(prefix);
		if(m_endpoints.containsKey(prefix))
			throw new EServiceEndpointAlreadyRegistered(prefix);
		
		IServiceInvoker endpoint;
		if(serviceEndpoint instanceof IServiceInvoker)
			endpoint = (IServiceInvoker) serviceEndpoint;
		else 
			endpoint = new RequestHandler.ReflectServiceInvoker(serviceEndpoint);
		m_endpoints.put(prefix, endpoint);
	}
	
	static class MethodCall{
		String m_prefix;
		String m_method;
	}

	public Object invokeService(String method, Object... args) {
		MethodCall mc = parseMethod(method);
		IServiceInvoker ep = m_endpoints.get(mc.m_prefix);
		if(null == ep)
			throw new EServiceEnpointNotFound(mc.m_prefix);
		return ep.invokeService(mc.m_method, args);
	}

	private MethodCall parseMethod(String method) {
		int delimIdx = method.indexOf(DELIM);
		if(delimIdx < 0 || delimIdx == method.length()-1)
			throw new ESoap.EMethodNotFound(method);
		MethodCall res = new MethodCall();
		res.m_prefix = method.substring(0, delimIdx);
		res.m_method = method.substring(delimIdx + 1);
		return res;
	}

}
