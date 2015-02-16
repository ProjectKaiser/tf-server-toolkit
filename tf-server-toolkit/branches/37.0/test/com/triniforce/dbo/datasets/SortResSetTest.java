/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.Arrays;

import com.triniforce.db.test.TFTestCase;

public class SortResSetTest extends TFTestCase {

	public void testSortResSet() {
		SortResSet res = new SortResSet(Arrays.asList("col1","col2"));
		assertEquals(Arrays.asList("col1","col2"), res.getColumns());
		assertFalse(res.next());
	}

	public void testAddRow() {
		SortResSet res = new SortResSet(Arrays.asList("col1","col2"));
		res.addRow(new Object[]{1, "s1"});
		
		assertTrue(res.next());
		assertEquals("s1", res.getObject(2));
	}

	public void testSort() {
		SortResSet res = new SortResSet(Arrays.asList("col1","col2"));
		res.addRow(new Object[]{1, "s1"});
		res.addRow(new Object[]{4, "s2"});
		res.addRow(new Object[]{2, "s3"});
		res.addRow(new Object[]{7, "s4"});
		res.addRow(new Object[]{3, "s5"});
		
		res.sort(Arrays.asList((Object)"col1"));
		
		assertTrue(res.next());
		assertEquals("s1", res.getObject(2));
		assertTrue(res.next());
		assertEquals("s3", res.getObject(2));
		assertTrue(res.next());
		assertEquals("s5", res.getObject(2));
		assertTrue(res.next());
		assertEquals("s2", res.getObject(2));
		
	}

	public void testNext() {
		SortResSet res = new SortResSet(Arrays.asList("c1"));
		res.addRow(new Object[]{"s1"});	
		res.addRow(new Object[]{"s1"});	
		res.addRow(new Object[]{"s1"});
		
		res.sort(Arrays.asList((Object)"c1"));
		
		for(int i=0;i<3; i++)
			assertTrue(res.next());
		assertFalse(res.next());
		
	}

	public void testGetObject() {
		SortResSet res = new SortResSet(Arrays.asList("col1","col2"));
		res.addRow(new Object[]{1, "s1"});
		
		res.next();
		assertEquals("s1", res.getObject(2));
		assertEquals(1, res.getObject(1));
		
	}
}
