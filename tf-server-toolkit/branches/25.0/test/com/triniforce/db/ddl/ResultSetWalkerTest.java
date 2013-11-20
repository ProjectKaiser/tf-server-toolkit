/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.ddl.ResultSetWalker.IObjectFactory;
import com.triniforce.db.test.TFTestCase;

public class ResultSetWalkerTest extends TFTestCase {

	@SuppressWarnings("unchecked")
	public void testResultSetWalker() throws SQLException {
		Mockery ctx = new Mockery();
		final ResultSet rs = ctx.mock(ResultSet.class);
		final IObjectFactory<String> fact = ctx.mock(IObjectFactory.class);
		
		ctx.checking(new Expectations(){{
			allowing(rs).findColumn("f1"); will(returnValue(12));
			one(rs).next(); will(returnValue(true));
			one(rs).getObject(12);
			one(fact).createObject(rs);
		}});
		ResultSetWalker<String> walker = new ResultSetWalker<String>(fact, rs, "f1");
		ctx.assertIsSatisfied();
		
		assertTrue(walker.hasNext());
		
		ctx.checking(new Expectations(){{
			one(rs).next(); will(returnValue(false));
		}});
		walker = new ResultSetWalker<String>(fact, rs, "f1");
		ctx.assertIsSatisfied();
		
		assertFalse(walker.hasNext());	
		
		ctx.checking(new Expectations(){{
			one(rs).next(); will(returnValue(true));
			one(rs).getObject(12);
			one(fact).createObject(rs); will(returnValue(null));

			one(rs).next(); will(returnValue(true));
			one(fact).createObject(rs);
			one(rs).getObject(12);
			
		}});
		walker = new ResultSetWalker<String>(fact, rs, "f1");
		
		ctx.assertIsSatisfied();
		
	}

	@SuppressWarnings("unchecked")
	public void testHasNext() throws SQLException {

		
	}

	@SuppressWarnings("unchecked")
	public void testNext() throws SQLException {
		{
			Mockery ctx = new Mockery();
			final ResultSet rs = ctx.mock(ResultSet.class);
			final IObjectFactory<String> fact = ctx.mock(IObjectFactory.class);
			
			ctx.checking(new Expectations(){{
				allowing(rs).findColumn("f1"); will(returnValue(9));
				one(rs).next(); will(returnValue(true));
				one(rs).next(); will(returnValue(true));
				one(rs).next(); will(returnValue(true));
				one(rs).next(); will(returnValue(false));
				one(rs).getObject(9); will(returnValue("key1"));
				one(rs).getObject(9); will(returnValue("key1"));
				one(rs).getObject(9); will(returnValue("key2"));
				
				one(fact).createObject(rs); will(returnValue("string_1"));
				one(fact).addRow("string_1", rs);
				one(fact).addRow("string_1", rs);
				
				one(fact).createObject(rs); will(returnValue("string_2"));
				one(fact).addRow("string_2", rs);
			}});
			ResultSetWalker<String> walker = new ResultSetWalker<String>(fact, rs, "f1");
			
			assertTrue(walker.hasNext());
			assertEquals("string_1", walker.next());
			assertTrue(walker.hasNext());
			assertEquals("string_2", walker.next());
			assertFalse(walker.hasNext());
			
			ctx.assertIsSatisfied();
		}
		{
			Mockery ctx = new Mockery();
			final ResultSet rs = ctx.mock(ResultSet.class);
			final IObjectFactory<String> fact = ctx.mock(IObjectFactory.class);
			
			ctx.checking(new Expectations(){{
				allowing(rs).findColumn("f1"); will(returnValue(9));
				one(rs).next(); will(returnValue(false));
			}});
			ResultSetWalker<String> walker = new ResultSetWalker<String>(fact, rs, "f1");
			
			assertEquals(null, walker.next());
			assertEquals(null, walker.next());
			assertEquals(null, walker.next());
			
			ctx.assertIsSatisfied();
		}
	}

}
