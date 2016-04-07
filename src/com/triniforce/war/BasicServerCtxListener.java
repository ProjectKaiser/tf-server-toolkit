/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.TFUtils;

public class BasicServerCtxListener implements ServletContextListener {
	
	public final static String APPLICATION_NAME = "JServer";  
	
	static final String DEFAULT_CONFIG = "admin_password=";


	public static void configureLog4Java(String folder){
		
	}
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		InitialContext initContext;
		try {
			initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup(BasicServerServlet.CONTEXT);
			File homeFolder = new File((String)envContext.lookup(BasicServerServlet.HOME));
	
			getClass().getResourceAsStream("DefaultLog4j.properties");
			File flog4j = createFileIfNotExists(homeFolder, "log4j.properties",
					TFUtils.readResource(BeanShellExecutor.class, "DefaultLog4j.properties"));
			PropertyConfigurator.configure(flog4j.getAbsolutePath());
			
			createFileIfNotExists(homeFolder, "config.properties", DEFAULT_CONFIG);
		} catch (NamingException e1) {
			e1.printStackTrace();
		}
	}
	
	private File createFileIfNotExists(File homeFolder, String fname, String content) {
		File f = new File(homeFolder, fname);
		if(!f.exists()){
			FileOutputStream fout;
			try {
				fout = new FileOutputStream(f);
				try{
					ByteArrayInputStream log4jTemplate = new ByteArrayInputStream(content.getBytes());
					TFUtils.copyStream(log4jTemplate, fout);
					fout.flush();
					log4jTemplate.close();
				}finally{
					fout.close();
				}
			} catch (IOException e) {
				ApiAlgs.rethrowException(e);
			}
		}
		return f;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}


}
