/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.SQLException;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.qsync.impl.QSyncManager;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.server.plugins.kernel.BasicServer.ServerException;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISrvSmartTranFactory.ITranExtender;
import com.triniforce.utils.ApiStack;

public class SrvSmartTranFactoryTest extends ServerTest {
	/*
	@SuppressWarnings("unchecked")
	public void testRegisterTransactionInterface() throws Exception {
		BasicServer srv = new BasicServer(m_coreApi);
		srv.doRegistration();
		srv.enterMode(Mode.Registration);
		ISrvSmartTranFactory trf = ApiStack.getInterface(ISrvSmartTranFactory.class);
		try{
			Mockery ctx = new Mockery();
			final ITransationInterfaceFactory<String> f = ctx.mock(ISrvSmartTranFactory.ITransationInterfaceFactory.class);
			ctx.checking(new Expectations(){{
				one(f).createInterface(); will(returnValue("---Helloo---"));
			}});
			trf.registerTransactionInterface(String.class, f);
			trf.push();
			try{
				String v = ApiStack.getInterface(String.class);
				assertEquals("---Helloo---", v);
			}finally{
			}
			ctx.assertIsSatisfied();
		}finally{
			trf.pop();
			srv.leaveMode();
		}
	}*/
	
	public void testRegisterOuterExtender() throws Exception{
		BasicServer srv = new BasicServer(m_coreApi);
		srv.doRegistration();
		srv.enterMode(Mode.Registration);
		ApiStack.pushInterface(IQSyncManager.class, new QSyncManager());
		try{
			ISrvSmartTranFactory trf = ApiStack.getInterface(ISrvSmartTranFactory.class);
			Mockery ctx = new Mockery();
			final ITranExtender interceptor = ctx.mock(ISrvSmartTranFactory.ITranExtender.class);
			trf.registerOuterExtender(interceptor);
			{
				try{
					ctx.checking(new Expectations(){{
						one(interceptor).push();
					}});
					trf.push();
					ctx.assertIsSatisfied();
				}finally{
					ctx.checking(new Expectations(){{
						one(interceptor).pop(with(new TrnMatcher(true, false)));
					}});
					trf.pop();
					ctx.assertIsSatisfied();
				}
			}
			{
				ctx.checking(new Expectations(){{
					one(interceptor).push();
					one(interceptor).pop(with(new TrnMatcher(true, true)));
				}});
				trf.push();
				SrvApiAlgs2.getIServerTran().commit();
				trf.pop();
			}
		} finally{
			ApiStack.popInterface(1);
			srv.leaveMode();
			
		}
	}
	
	
	public void testRegisterInnerExtender() throws ServerException, EDBObjectException, SQLException{
		BasicServer srv = new BasicServer(m_coreApi);
		srv.doRegistration();
		srv.enterMode(Mode.Registration);
		ApiStack.pushInterface(IQSyncManager.class, new QSyncManager());
		try{
			ISrvSmartTranFactory trf = ApiStack.getInterface(ISrvSmartTranFactory.class);
			Mockery ctx = new Mockery();
			final ITranExtender interceptor = ctx.mock(ISrvSmartTranFactory.ITranExtender.class);
			trf.registerInnerExtender(interceptor);
			trf.registerInnerExtender(interceptor);
			{
				try{
					ctx.checking(new Expectations(){{
						one(interceptor).push();
						one(interceptor).push();
					}});
					trf.push();
					ctx.assertIsSatisfied();
				}finally{
					ctx.checking(new Expectations(){{
						one(interceptor).pop(with(new TrnMatcher(false, false)));
						one(interceptor).pop(with(new TrnMatcher(false, false)));
					}});
					trf.pop();
					ctx.assertIsSatisfied();
				}
			}
			{
				ctx.checking(new Expectations(){{
					one(interceptor).push();
					one(interceptor).push();
					one(interceptor).pop(with(new TrnMatcher(false, true)));
					one(interceptor).pop(with(new TrnMatcher(false, true)));
				}});
				trf.push();
				SrvApiAlgs2.getIServerTran().commit();
				trf.pop();
			}
		} finally{
			ApiStack.popInterface(1);
			srv.leaveMode();
			
		}		
	}
	
	static class TrnMatcher extends TypeSafeMatcher<Boolean>{
		private boolean m_bClosed;
		private boolean m_bCommit;
		private String m_text;

		public TrnMatcher(boolean bClosed, boolean bCommit) {
			m_bClosed = bClosed;
			m_bCommit = bCommit;
		}

		@Override
		public boolean matchesSafely(Boolean item) {
			if(item == m_bCommit){
				boolean trState = SrvApiAlgs2.getIServerTran().isClosed();
				m_text = "transaction is " + trState; 
				return trState == m_bClosed;
			}
			m_text = "bCommit is "+ item;
			return false;
		}

		public void describeTo(Description description) {
			description.appendText(m_text);
			
		}
		
	}
}
;