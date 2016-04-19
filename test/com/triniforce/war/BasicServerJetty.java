/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.utils.TFUtils;

public class BasicServerJetty {
	public final int PORT;

	private Server server = null;

	public BasicServerJetty(int port) {
		PORT = port;
	}

	public static void main(String[] args) throws Exception {
		BasicServerJetty srv = new BasicServerJetty(args.length > 0 ? Integer.parseInt(args[0]) : 8888);
		try {
			if(args.length > 1){
				Class<? extends IPlugin> clsPlugin = (Class<? extends IPlugin>) Class.forName(args[1]);
				IPlugin plugin = clsPlugin.newInstance();
				srv.startWebServer(plugin);
			}
			else
				srv.startWebServer();
			Handler[] hs = srv.server.getHandlers();
			for(Handler h : hs){
				System.out.println(h.getClass());
			}
			srv.server.join();
		} finally {
			srv.stopWebServer();
		}
	}

	private void startWebServer(IPlugin plugin) throws Exception {
		if (server != null) {
			throw new IllegalStateException("startWebServer() can not be called twice");
		}

		server = new Server(PORT);
		
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", 
				"org.eclipse.jetty.plus.webapp.EnvConfiguration", 
				"org.eclipse.jetty.plus.webapp.PlusConfiguration");

		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
		context.setResourceBase("war");
		context.setParentLoaderPriority(true);

		server.setHandler(context);

		System.setProperty("catalina.home", new File(".").getAbsolutePath());
		
		File fhome = new File(TFTestCase.getTfTestFolder(), "tftool");
		TFUtils.copyStream(BasicServerJetty.class.getResourceAsStream("test_pligin.jar"), new File(new File(fhome, "plugins"), "test_pligin.jar"));
		
		new org.eclipse.jetty.plus.jndi.Resource(server, "BasicServerDb", TFTestCase.getDataSource());
		new org.eclipse.jetty.plus.jndi.Resource(server, "BasicServerHome",fhome.getAbsolutePath());
		
		new org.eclipse.jetty.plus.jndi.Resource(server, "BasicServerPlugins",Arrays.asList(plugin));

		server.start();
	}

	public void startWebServer() throws Exception {
		if (server != null) {
			throw new IllegalStateException("startWebServer() can not be called twice");
		}

		server = new Server(PORT);
		
		org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
		classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", 
				"org.eclipse.jetty.plus.webapp.EnvConfiguration", 
				"org.eclipse.jetty.plus.webapp.PlusConfiguration");

		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
		context.setResourceBase("war");
		context.setParentLoaderPriority(true);

		server.setHandler(context);

		System.setProperty("catalina.home", new File(".").getAbsolutePath());
		
		File fhome = new File(TFTestCase.getTfTestFolder(), "tftool");
		TFUtils.copyStream(BasicServerJetty.class.getResourceAsStream("test_pligin.jar"), new File(new File(fhome, "plugins"), "test_pligin.jar"));
		
		new org.eclipse.jetty.plus.jndi.Resource(server, "BasicServerDb", TFTestCase.getDataSource());
		new org.eclipse.jetty.plus.jndi.Resource(server, "BasicServerHome",fhome.getAbsolutePath());

		server.start();
	}

	public Server getServer(){
		return server;
	}

	public void stopWebServer() throws Exception {
		if (server != null) {
			server.stop();
			server = null;
		}
	}
}

