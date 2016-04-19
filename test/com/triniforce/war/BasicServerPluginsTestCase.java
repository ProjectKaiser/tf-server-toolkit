/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.File;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.PluginsLoader;
import com.triniforce.server.srvapi.IPlugin;

public class BasicServerPluginsTestCase extends BasicServerTestCase {

	@Override
	protected void setUp() throws Exception {
		addPlugin(new TFToolsPlugin());
		PluginsLoader plgLoader = new PluginsLoader(new File(getTfTestFolder(), BasicServerServlet.PLUGINS_FOLDER));
		for (IPlugin plugin : plgLoader.loadPlugins()) {
			addPlugin(plugin);
		}
		super.setUp();
	}
}
