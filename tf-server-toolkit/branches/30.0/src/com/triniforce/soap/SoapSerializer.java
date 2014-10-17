/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SoapSerializer {

	public void serialize(InterfaceDescription desc, Object obj, OutputStream out) throws IOException{
	}
	
	public Object deserialize(InterfaceDescription desc, InputStream source){
		return null;
	}

	public void serializeError(InterfaceDescription desc, Throwable t, OutputStream out) throws IOException {
	}
}
