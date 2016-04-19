/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;

import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.extensions.PluginsLoader;
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
import com.triniforce.utils.TFUtils;

public class BasicServerServlet extends SOAPServlet {
	private static final long serialVersionUID = 1947781333414814776L;
	static final String INITPARAM_PLUGINS = "plugins";
	private static final int MAX_ACTIVE_CONNECTIONS = 20;
	
	static final String CONTEXT = "java:/comp/env";
	static final String DATABASE = "BasicServerDb";
	static final String HOME = "BasicServerHome";
	static final String PLUGINS_ENV = "BasicServerPlugins";
	static final String PLUGINS_FOLDER = "plugins";
	private static final String SRV_PARAMS_FILE = "config.properties";
	
	List<IPlugin> m_plugins = new ArrayList<IPlugin>();
	IPooledConnection m_pool;
	private BasicServer m_server;
	
	public BasicServerServlet() {
		super("http://soap.tftool.untill.eu/", "tftool", BasicServerServlet.class.getPackage());
	}
	

	
	@Override
	public void init(ServletConfig arg0) throws ServletException {
		try {
			InitialContext initContext;
			initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup(CONTEXT);
			File homeFolder = new File((String)envContext.lookup(HOME));
			
			if(!homeFolder.isDirectory()){
				ApiAlgs.getLog(this).error("Home folder is not directory");
				return;
			}
			
			TFToolsPlugin plugin1 = new TFToolsPlugin();
			plugin1.addServiceExtension(new BasicServerConfig(new File(homeFolder, SRV_PARAMS_FILE).getAbsolutePath()));
			m_plugins.add(plugin1);
			
			PluginsLoader plgLoader = new PluginsLoader(new File(homeFolder, PLUGINS_FOLDER));
			m_plugins.addAll(plgLoader.loadPlugins());
			
			try{
				Object envPlgs = envContext.lookup(PLUGINS_ENV);
				m_plugins.addAll((Collection<? extends IPlugin>)envPlgs);
			}catch(NamingException e){
			}

			BasicDataSource ds;
			try{
				ds = (BasicDataSource)envContext.lookup(DATABASE);
			}catch(NamingException e){
				Properties props = new Properties();
				props.put("driverClassName", "org.apache.derby.jdbc.EmbeddedDriver");
				File fDb = new File(homeFolder, "DB");
				props.put("url", "jdbc:derby:"+fDb.getAbsolutePath()+";create=true");
				ds = (BasicDataSource) BasicDataSourceFactory.createDataSource(props);
			}
			m_pool = new TFPooledConnection(ds, MAX_ACTIVE_CONNECTIONS);
		} catch (NamingException e) {
			ApiAlgs.rethrowException(e);
		} catch (Exception e) {
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
			BasicServerInvoker res = new BasicServerInvoker(m_server, multiSvc);
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
	
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if("wsdl".equals(request.getQueryString()))
			super.doGet(request, response);
		else
			TFUtils.copyStream(getClass().getResourceAsStream("bs.htm"), response.getOutputStream());
	}

}
