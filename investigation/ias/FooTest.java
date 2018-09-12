/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import com.triniforce.db.test.TFTestCase;

public class FooTest extends TFTestCase {

	@Override
	public void test() throws Exception {
		try{
			throw new OutOfMemoryError();
		}catch(Throwable e){}
	}
}
