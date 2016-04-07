/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.war.api.IBasicServerConfig;

public class BasicServerConfig implements IBasicServerConfig, IPKEPAPI {
	
	private String m_config;

	public BasicServerConfig(String config) {
		m_config = config;
	}

	@Override
	public Class getImplementedInterface() {
		return IBasicServerConfig.class;
	}
	
	public Properties getProperties(){
		Properties props = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(m_config);
			props.load(in);
			in.close();
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		}
		return props;
		
	}

}
