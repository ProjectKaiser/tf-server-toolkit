/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap.testpkg_01;

import com.triniforce.soap.SoapInclude;

@SoapInclude(extraClasses = {ITestHorse.CTail.class})
public interface ITestHorse {
	static class CTail{}

	void run(Hand hand);
}
