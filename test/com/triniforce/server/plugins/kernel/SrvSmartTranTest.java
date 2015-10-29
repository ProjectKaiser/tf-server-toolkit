/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.DBTestCase;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.ISrvSmartTranFactory.ITranExtender;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class SrvSmartTranTest extends DBTestCase{

	public void testSrvSmartTran() throws Exception {
		Connection con = getConnection();
		Mockery ctx = new Mockery();
		final ISrvSmartTranFactory trf = ctx.mock(ISrvSmartTranFactory.class);
		final ITranExtender tre = ctx.mock(ITranExtender.class);
		final Log log = ctx.mock(Log.class);
		Api api = new Api();
		api.setIntfImplementor(ISrvSmartTranFactory.class, trf);
		api.setIntfImplementor(LogFactory.class, new ServerImplTest.TestLogFactory(log));
		ApiStack.pushApi(api);
		try{
			ctx.checking(new Expectations(){{
				//one(trf).getInnerExtenders(); will(returnValue(Arrays.asList(tre, tre)));
				one(trf).getOuterExtenders(); will(returnValue(Arrays.asList(tre, tre)));
				
				RuntimeException e1 = new RuntimeException("alarm-alarm1");
				//RuntimeException e2 = new RuntimeException("alarm-alarm2");

				one(tre).push();
				one(tre).push(); will(throwException(e1));
				//one(tre).push(); will(throwException(e2));
				//one(tre).push();
				
				//one(log).error(e1);
				//one(log).error(e2);
			}});
			
			try{
				new SrvSmartTran(con, null);
				fail();
			}catch(RuntimeException e){
			}
			
			ctx.assertIsSatisfied();
		}finally{
			ApiStack.popApi();
		}
	}
	
	
	public void testCloseBoolean() throws Exception {
		Connection con = getConnection();
		{
			Mockery ctx = new Mockery();
			final ISrvSmartTranFactory trf = ctx.mock(ISrvSmartTranFactory.class);
			final ITranExtender tre = ctx.mock(ITranExtender.class);
			final Log log = ctx.mock(Log.class);
			final IQSyncManager sm = ctx.mock(IQSyncManager.class);
			Api api = new Api();
			api.setIntfImplementor(ISrvSmartTranFactory.class, trf);
			api.setIntfImplementor(IQSyncManager.class, sm);
			api.setIntfImplementor(LogFactory.class, new ServerImplTest.TestLogFactory(log));
			ApiStack.pushApi(api);
			try{
				ctx.checking(new Expectations(){{
					one(trf).getInnerExtenders();
					one(trf).getOuterExtenders();
				}});
				
				SrvSmartTran trn = new SrvSmartTran(con, null);
				
				final RuntimeException e1 = new RuntimeException("alarm-alarm1");			
				
				ctx.checking(new Expectations(){{
					one(trf).getInnerExtenders(); will(returnValue(Arrays.asList(tre, tre)));
					one(trf).getOuterExtenders(); will(returnValue(Arrays.asList(tre, tre)));
					
					RuntimeException e2 = new RuntimeException("alarm-alarm2");
					one(tre).pop(true);
					one(tre).pop(true); will(throwException(e1));
					one(tre).pop(false); will(throwException(e2));
					one(tre).pop(false);
					
					one(log).warn(with(any(String.class)), with(any(RuntimeException.class)));
					one(log).error(with(any(String.class)), with(any(RuntimeException.class)));
					//one(log).error(e2);
				}});
				
				try{
					trn.commit();
				} catch(RuntimeException e){
					assertEquals(e1, e);
				}
				
				
				ctx.assertIsSatisfied();
			}finally{
				ApiStack.popApi();
			}
		}
		
		{
			//Affect for IQSyncManager
			Mockery ctx = new Mockery();
			final ISrvSmartTranFactory trf = ctx.mock(ISrvSmartTranFactory.class);
			final Log log = ctx.mock(Log.class);
			final IQSyncManager sm = ctx.mock(IQSyncManager.class);
			Api api = new Api();
			final ISrvSmartTran trnExt = ctx.mock(ISrvSmartTran.class);
			api.setIntfImplementor(ISrvSmartTranFactory.class, trf);
			api.setIntfImplementor(LogFactory.class, new ServerImplTest.TestLogFactory(log));
			api.setIntfImplementor(IQSyncManager.class, sm);
			api.setIntfImplementor(ISrvSmartTran.class, trnExt);
			ApiStack.pushApi(api);
			try{
				ctx.checking(new Expectations(){{
					ignoring(trf);
					ignoring(trnExt);
				}});
				final IDbQueue fq = ctx.mock(IDbQueue.class);
				{
					SrvSmartTran trn = new SrvSmartTran(con, null);
					
					trn.registerAffectedQueue(fq);
					ctx.checking(new Expectations(){{
						one(fq).getId(); will(returnValue(78587L));
						one(sm).onQueueChanged(78587L);
					}});
					trn.commit();
					
					ctx.assertIsSatisfied();
				}
				
				{
					SrvSmartTran trn = new SrvSmartTran(con, null);
					trn.registerAffectedQueue(fq);
					trn.registerAffectedQueue(fq);
					trn.close(); // rollback
					ctx.assertIsSatisfied();
				}
			}finally{
				ApiStack.popApi();
			}	
		}
	}
}
