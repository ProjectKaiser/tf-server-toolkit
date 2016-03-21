/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.dbcp.BasicDataSource;

import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.server.plugins.kernel.BasicServer;
import com.triniforce.server.plugins.kernel.TFPooledConnection;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.soap.InterfaceDescription;
import com.triniforce.soap.InterfaceOperationDescription;
import com.triniforce.soap.MultiClassServiceInvoker;
import com.triniforce.soap.RequestHandler;
import com.triniforce.soap.RequestHandler.ReflectServiceInvoker;
import com.triniforce.soap.SOAPServlet;
import com.triniforce.soap.SoapInclude;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;

public class TFToolsServlet extends SOAPServlet {
	private static final long serialVersionUID = 1947781333414814776L;
	static final String INITPARAM_PLUGINS = "plugins";
	private static final int MAX_ACTIVE_CONNECTIONS = 20;
	
	static final String CONTEXT = "java:/comp/env";
	static final String DATABASE = "tftoolDb";
	
	List<Class<? extends IPlugin>> m_endpoints;
	List<IPlugin> m_plugins;
	IPooledConnection m_pool;
	private BasicServer m_server;
	
	public TFToolsServlet() {
		super("http://soap.tftool.untill.eu/", "tftool", TFToolsServlet.class.getPackage());
	}
	

	
	@Override
	public void init(ServletConfig arg0) throws ServletException {
		m_endpoints = new ArrayList<Class<?extends IPlugin>>();
		m_plugins = new ArrayList<IPlugin>();
		m_plugins.add(new TFToolsPlugin());
		String epts = arg0.getInitParameter(INITPARAM_PLUGINS);
		if(null == epts){
			ApiAlgs.getLog(this).warn("No plugins extensions");			
		}
		else{
			for(String clsName : epts.split(",")){
				try {
					Class<? extends IPlugin> cls = (Class<? extends IPlugin>) Class.forName(clsName);
					IPlugin plugin = cls.newInstance();
					m_endpoints.add(cls);
					m_plugins.add(plugin);
				} catch (ClassNotFoundException e) {
					ApiAlgs.getLog(this).error("Classs endpoint not found : " + clsName, e);
				} catch (InstantiationException e) {
					ApiAlgs.getLog(this).error("Plugin instanciation error", e);
				} catch (IllegalAccessException e) {
					ApiAlgs.getLog(this).error("Plugin instanciation error", e);
				}
			}
		}

		InitialContext initContext;
		try {
			initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup(CONTEXT);
			BasicDataSource ds = (BasicDataSource)envContext.lookup(DATABASE);
			m_pool = new TFPooledConnection(ds, MAX_ACTIVE_CONNECTIONS);
		} catch (NamingException e) {
			ApiAlgs.rethrowException(e);
		}
		
		super.init(arg0);
	}

	@Override
	public Object createService() {
		try {
			m_server = initServer();
			IPKExtensionPoint svcPoint = m_server.getExtensionPoint(UEPServiceEndoint.class);
			
			MultiClassServiceInvoker multiSvc = new MultiClassServiceInvoker();
			for(Map.Entry<String, IPKExtension> entry : svcPoint.getExtensions().entrySet()){
				String[] split = entry.getKey().split("\\.");
				String pfx = split.length > 1 ? split[split.length-2] : "";;
				Object svc;
				svc = entry.getValue().getInstance();
				ReflectServiceInvoker invoker = new RequestHandler.ReflectServiceInvoker(svc);
				multiSvc.addServiceEndpoint(pfx, invoker);
			}
			TFToolsBasicServerInvoker res = new TFToolsBasicServerInvoker(m_server, multiSvc);
			return res;
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
			return null;
		}
	}

	BasicServer initServer(){
		try{
			Api coreApi = new Api();
			coreApi.setIntfImplementor(IPooledConnection.class, m_pool);
			
			BasicServer srv = new BasicServer(coreApi, m_plugins);
			srv.doRegistration();
	        if(srv.isDbModificationNeeded()){
	        	srv.doDbModification();
	        }
	        srv.initAndStart();
			return srv;
		} catch(Exception e){
			ApiAlgs.rethrowException(e);
			return null;
		}
	}


	@Override
	protected InterfaceDescription generateInterfaceDescription(
			InterfaceDescription oldDesc) {
		
		try {
			IPKExtensionPoint svcPoint = m_server.getExtensionPoint(UEPServiceEndoint.class);
			
			InterfaceDescription res;
			ArrayList<InterfaceOperationDescription> ops = new ArrayList<InterfaceOperationDescription>();
			List<SoapInclude> soapIncls = new ArrayList<SoapInclude>();

			for(Map.Entry<String, IPKExtension> entry : svcPoint.getExtensions().entrySet()){
				Object svc;
				svc = entry.getValue().getInstance();
				Class<? extends Object> cls = svc.getClass();
				List<InterfaceOperationDescription> endpointOps = m_gen.listInterfaceOperations(
						cls, true);
				ops.addAll(endpointOps);
				SoapInclude anno = cls.getAnnotation(SoapInclude.class);
				if (null != anno)
					soapIncls.add(anno);
			}
			res = m_gen.parse(oldDesc, ops, getClass().getPackage(), soapIncls);
			return res;
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
			return null;
		}		
	}

}
