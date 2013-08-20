/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.Arrays;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.soap.WhereExpr;

public class PipeResSetTest extends TFTestCase {
	
	public void testPipeResSet(){
		PipeResSet rs = new PipeResSet(CVRHandlerTest.getRS());
		assertTrue(rs.next());
		assertEquals(1, rs.getObject(1));
		
	}

	public void testAddFilter(){
		PipeResSet rs = new PipeResSet(CVRHandlerTest.getRS());
		rs.addFilter("idx", 4);
		assertTrue(rs.next());
		assertEquals(4, rs.getObject(1));
	}
	
	public void testAddFilterEval(){
		PipeResSet rs = new PipeResSet(CVRHandlerTest.getRS());
		rs.addFilter(Arrays.asList((WhereExpr)new WhereExpr.ExprBetween("price", 20.00, 70.00)));
		
		assertTrue(rs.next());
		assertEquals("string_05", rs.getObject(2));
		
	}
	
	static class TestFF extends FieldFunction{
		@Override
		public Object exec(Object value) {
			return value.toString();
		}
	}
	
	public void testAddFieldFunction(){
		PipeResSet rs = new PipeResSet(CVRHandlerTest.getRS());
		rs.addFieldFunction("papper", "price", new TestFF());
		assertEquals("papper", rs.getColumns().get(3));
		
		rs.next();
		assertEquals("99.99", rs.getObject(4));
		rs.next();
		assertEquals("9.99", rs.getObject(4));

		rs = new PipeResSet(CVRHandlerTest.getRS());
		rs.addFieldFunction("papper", "price", new TestFF());
		try{
			rs.addFieldFunction("papper", "price", new TestFF());
			fail();
		}catch(EColumnAlreadyAdded e){}
		assertEquals(Arrays.asList("idx", "name", "price", "papper"), rs.getColumns());
	}
	
}
