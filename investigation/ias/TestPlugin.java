/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import com.triniforce.extensions.PKPlugin;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;
import com.triniforce.war.UEPServiceEndoint;

public class TestPlugin extends PKPlugin {
	
	public static class TestSvc{
		public long systemTime(){
			return ApiStack.getInterface(ITime.class).currentTimeMillis();
			
		}
	}

	@Override
	public void doRegistration() {

	}

	@Override
	public void doExtensionPointsRegistration() {
		putExtension(UEPServiceEndoint.class, TestSvc.class);
	}

}
