/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

import com.triniforce.db.test.TFTestCase;

public class UniqueNameGeneratorTest extends TFTestCase {

	public void testGenerate() {
		UniqueNameGenerator ng = new UniqueNameGenerator();
		assertEquals("VVV", ng.generate("VVV"));
		assertEquals("WWW", ng.generate("WWW"));
		
		assertTrue(ng.contains("VVV"));
		assertTrue(ng.contains("VvV"));
		assertFalse(ng.contains("VV"));
		
		assertEquals("VVV1", ng.generate("VVV"));
		assertEquals("VVV2", ng.generate("VVV2"));
		assertEquals("VVV11", ng.generate("VVV1"));
		assertEquals("VVV3", ng.generate("VVV"));		
		assertEquals("VVV12", ng.generate("VVV1"));
	}

}
