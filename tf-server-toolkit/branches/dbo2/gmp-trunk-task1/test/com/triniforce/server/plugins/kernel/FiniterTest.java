/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.util.Collections;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.BasicServerCorePlugin.FiniterExtender;
import com.triniforce.server.plugins.kernel.BasicServerCorePlugin.FiniterExtender.Finiter;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IFiniter;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IFinitable;
import com.triniforce.utils.IFinitableWithRollback;

public class FiniterTest extends TFTestCase {

	int nCall = 0;

	int nRb = 0;

	class TestFinitable implements IFinitable {
		public void finit() {
			nCall++;
		}
	}

	class TestFinitableWithRb implements IFinitableWithRollback {
		public void finit() {
			nCall++;
		}

		public void rollback() {
			nRb++;
		}
	}

	public void testRegisterFiniter() {
		{
			Finiter finiter = new Finiter();
			Mockery ctx = new Mockery();
			final IFinitable fable = ctx.mock(IFinitable.class);
			finiter.registerFiniter(fable);
			ctx.checking(new Expectations() {
				{
					one(fable).finit();
				}
			});
			finiter.flush(true);
			ctx.assertIsSatisfied();
		}
		{
			Finiter finiter = new Finiter();
			finiter.registerFiniter(new TestFinitable());
			finiter.registerFiniter(new TestFinitable());
			finiter.registerFiniter(new TestFinitable());
			finiter.flush(true);
			assertEquals(3, nCall);
		}
	}

	public void testGetRegisteredFiniters() {
		Finiter finiter = new Finiter();
		assertEquals(Collections.emptySet(), finiter.getRegisteredFiniters());
		finiter.registerFiniter(new TestFinitable());
		finiter.registerFiniter(new TestFinitable());
		finiter.registerFiniter(new TestFinitable());
		assertEquals(3, finiter.getRegisteredFiniters().size());
	}

	static class TestEFinitable implements IFinitable {

		public void finit() {
			throw new RuntimeException();
		}

	}

	public void testFlush() {
		{
			Finiter finiter = new Finiter();
			finiter.registerFiniter(new TestFinitable());
			finiter.registerFiniter(new TestFinitableWithRb());
			finiter.registerFiniter(new TestFinitable());
			finiter.registerFiniter(new TestFinitableWithRb());
			finiter.registerFiniter(new TestFinitable());

			finiter.flush(true);
			assertEquals(5, nCall);
			assertEquals(0, nRb);

			nRb = 0;
			nCall = 0;

			finiter.flush(false);
			assertEquals(0, nCall);
			assertEquals(2, nRb);
		}
		{
			Finiter finiter = new Finiter();
			finiter.registerFiniter(new TestFinitable());
			finiter.registerFiniter(new TestFinitableWithRb());
			finiter.registerFiniter(new TestFinitable());
			finiter.registerFiniter(new TestEFinitable()); // <<-- exception
			finiter.registerFiniter(new TestFinitableWithRb());
			finiter.registerFiniter(new TestFinitable());

			nRb = 0;
			nCall = 0;
			finiter.flush(true);
			assertEquals(5, nCall);
			assertEquals(0, nRb);
		}
	}

	public void testFiniterEerxternder() {
		{
			FiniterExtender ext = new FiniterExtender();
			ext.push();
			try{
				IFiniter finiter = ApiStack.getApi().queryIntfImplementor(IFiniter.class);
				assertNotNull(finiter);
				finiter.registerFiniter(new TestFinitable());
				finiter.registerFiniter(new TestFinitableWithRb());
			}finally{
				ext.pop(true);
				assertNull(ApiStack.getApi().queryIntfImplementor(IFiniter.class));
				assertEquals(2, nCall);
				assertEquals(0, nRb);
			}
		}
		{
			nRb = 0;
			nCall = 0;
			FiniterExtender ext = new FiniterExtender();
			ext.push();
			try{
				IFiniter finiter = ApiStack.getApi().queryIntfImplementor(IFiniter.class);
				assertNotNull(finiter);
				finiter.registerFiniter(new TestFinitable());
				finiter.registerFiniter(new TestFinitableWithRb());
			}finally{
				ext.pop(false);
				assertNull(ApiStack.getApi().queryIntfImplementor(IFiniter.class));
				assertEquals(0, nCall);
				assertEquals(1, nRb);
			}
		}
		
	}

}
