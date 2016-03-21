/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.triniforce.db.test.TFTestCase;

public class TFToolsJetty {
	public static final int PORT = 8888;

	private static Server server = null;

	public static void main(String[] args) throws Exception {
		try {
			startWebServer();
			Handler[] hs = server.getHandlers();
			for(Handler h : hs){
				System.out.println(h.getClass());
			}
			server.join();
		} finally {
			stopWebServer();
		}
	}

	public static void startWebServer() throws Exception {
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
		
		new org.eclipse.jetty.plus.jndi.Resource(server, "tftoolDb", TFTestCase.getDataSource());

		server.start();
	}

	public static Server getServer(){
		return server;
	}

	public static void stopWebServer() throws Exception {
		if (server != null) {
			server.stop();
			server = null;
		}
	}
}

