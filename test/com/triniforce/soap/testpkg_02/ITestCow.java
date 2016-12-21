/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap.testpkg_02;

import com.triniforce.soap.ESoap.EParameterizedException;

public interface ITestCow {
	static class Hand{}
	
	static class EMyow extends EParameterizedException{
		public EMyow(String message, Throwable cause, String subcode) {
			super(null, null, null);
		}

		private static final long serialVersionUID = 1L;
		
	}
	
	void step(Hand hand) throws EMyow;
}
