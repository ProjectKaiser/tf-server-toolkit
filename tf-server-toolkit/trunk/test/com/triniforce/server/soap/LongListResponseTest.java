/* 

 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.soap;

import com.triniforce.db.test.TFTestCase;

public class LongListResponseTest extends TFTestCase {
	@Override
	public void test() throws Exception {
		LongListResponse llr = new LongListResponse("c1", "c2");
		llr.addRow();
		llr.setCell(-1, "c1", "v1");
		llr.setCell(-1, "c2", "v2");
		assertEquals("v1", llr.getCell(0, "c1"));
		assertEquals("v2", llr.getCell(0, "c2"));
	}

}
