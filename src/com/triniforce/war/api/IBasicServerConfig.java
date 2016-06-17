/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war.api;

import java.util.Properties;


public interface IBasicServerConfig {
	static final String SCRIPT_PWD_KEY = "admin_password";


	Properties getProperties();

	String getHomeFolder();
}
