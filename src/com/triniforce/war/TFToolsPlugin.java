/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import com.triniforce.server.TFPlugin;


public class TFToolsPlugin extends TFPlugin {

	@Override
	public void doRegistration() {
	}

	@Override
	public void doExtensionPointsRegistration() {
		putExtensionPoint(new com.triniforce.war.UEPServiceEndoint());
	}

}
