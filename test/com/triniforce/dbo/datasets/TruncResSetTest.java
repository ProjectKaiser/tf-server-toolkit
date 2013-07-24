/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.triniforce.db.dml.IResSet;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.dbo.datasets.TruncResSet.EColumnNotFound;

public class TruncResSetTest extends TFTestCase {

	public void testTruncResSet() {
		TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
		assertEquals(Collections.EMPTY_LIST, res.getColumns());
		
		try{
			res.addColumn("col_002");
			fail();
		}catch(EColumnNotFound e){
			
		}
	}

	public void testAddColumn() {
		TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
		res.addColumn("idx");
		assertEquals(Arrays.asList("idx"), res.getColumns());
		
		try{
			res.addColumn("col_001");
			fail();
		}catch(EColumnNotFound e){
			
		}
	}

	public void testSetFromBorder() {
		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			res.setFromBorder(8);
			assertFalse(res.next());
		}
		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			res.setFromBorder(2);
			for(int i=0;i<5; i++)
				assertTrue(res.next());
			assertFalse(res.next());
		}

		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			res.addColumn("idx");
			res.setFromBorder(2);
			res.next();
			assertEquals(3, res.getObject(1));
		}
	}

	public void testSetToBorder() {
		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			res.setToBorder(1);
			assertTrue(res.next());
			assertFalse(res.next());
		}
		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			assertTrue(res.next());
			assertTrue(res.next());
		}

	}

	public void testNext() {
		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			assertTrue(res.next());
		}
		{
			TruncResSet res = new TruncResSet(emptyRS());
			assertFalse(res.next());
		}
	}

	private IResSet emptyRS() {
		return new IResSet() {
			
			public boolean next() {
				return false;
			}
			
			public Object getObject(int columnIndex) throws IndexOutOfBoundsException {
				fail();
				return null;
			}
			
			public List<String> getColumns() {
				return Arrays.asList("col");
			}

            public Object getSoapObject(int columnIndex){
                return getObject(columnIndex);

            }
		};
	}

	public void testGetObject() {
		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			res.addColumn("idx");
			res.addColumn("name");
			assertTrue(res.next());
			assertEquals(1, res.getObject(1));
			assertEquals("string_01", res.getObject(2));
		}
		{
			TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
			res.addColumn("name");
			res.addColumn("idx");
			assertTrue(res.next());
			assertEquals("string_01", res.getObject(1));
			assertEquals(1, res.getObject(2));
		}
		
	}

	public void testGetColumns() {
		TruncResSet res = new TruncResSet(CVRHandlerTest.getRS());
		res.addColumn("name");
		res.addColumn("idx");
		res.addColumn("name");
		assertEquals(Arrays.asList("name", "idx", "name"), res.getColumns());
	}

}
