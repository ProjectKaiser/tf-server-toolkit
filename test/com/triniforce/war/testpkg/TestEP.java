/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war.testpkg;

import com.triniforce.extensions.PKPlugin;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;
import com.triniforce.war.UEPServiceEndoint;

public class TestEP extends PKPlugin{
	
	public static class TestSvc implements ISvc1{
		public void method_001(){
			ApiAlgs.getLog(this).info("log me");
		}

		public long method_002(){
			return ApiStack.getInterface(ITime.class).currentTimeMillis();
		}

		@Override
		public String method1(int a) {
			return "TESTRES: " + a;
		}
		
	} 

	@Override
	public void doRegistration() {
		putExtension(UEPServiceEndoint.class, TestSvc.class);
	}

	@Override
	public void doExtensionPointsRegistration() {
	}
}