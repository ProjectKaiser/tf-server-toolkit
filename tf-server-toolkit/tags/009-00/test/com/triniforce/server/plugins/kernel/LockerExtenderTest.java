/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.BasicServerCorePlugin.LockerExtender;
import com.triniforce.server.plugins.kernel.BasicServerCorePlugin.LockerExtender.Locker;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IFiniter;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker.ILockableObject;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker.LockerKey;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker.LockerValue;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap.IFactory;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class LockerExtenderTest extends TFTestCase {
	
	static class TestLV extends LockerValue{
		private boolean isLocked=false;
		@Override
		public void lock(LockerKey key) {
			super.lock(key);
			isLocked = true;
		}
		@Override
		public void finit() {
			super.finit();
			isLocked = false;
		}
	}

    public static class StringLocker implements ILockableObject{
        private final String m_value;

        public StringLocker(String value) {
            m_value = value;
        }
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((m_value == null) ? 0 : m_value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final StringLocker other = (StringLocker) obj;
            if (m_value == null) {
                if (other.m_value != null)
                    return false;
            } else if (!m_value.equals(other.m_value))
                return false;
            return true;
        }
        
    }
    
	@Override
	public void test() throws Exception {
		Mockery ctx = new Mockery();
		final IRefCountHashMap cmap = ctx.mock(IRefCountHashMap.class);
		final IFactory factory = ctx.mock(IRefCountHashMap.IFactory.class);
		final IFiniter finiter = ctx.mock(IFiniter.class);
		Api api = new Api();
		api.setIntfImplementor(IRefCountHashMap.class, cmap);
		api.setIntfImplementor(IFiniter.class, finiter);
		Locker locker = new BasicServerCorePlugin.LockerExtender.Locker(factory);
		final TestLV lv1 = new TestLV();
		final TestLV lv2 = new TestLV();
		ApiStack.pushApi(api);
		try{
			ctx.checking(new Expectations(){{
				one(cmap).put(new LockerKey(new StringLocker("key_1")), factory);
				will(returnValue(lv1));
				one(cmap).put(new LockerKey(new StringLocker("key_2")), factory);
				will(returnValue(lv2));
				one(finiter).registerFiniter(lv1);
				one(finiter).registerFiniter(lv2);
			}});
			locker.lock(new StringLocker("key_1"));
			locker.lock(new StringLocker("key_2"));
			assertTrue(lv1.isLocked);
			assertTrue(lv2.isLocked);
			ctx.assertIsSatisfied();
		} finally{
			lv1.finit();
			lv2.finit();
			ApiStack.popApi();
		}
	}
	
	public void testExtender(){
		LockerExtender ext = new BasicServerCorePlugin.LockerExtender();
		ext.push();
		try{
			Locker locker = ApiStack.getApi().queryIntfImplementor(ILocker.class);
			assertNotNull(locker);
		}finally{
			ext.pop(false);
			assertNull(ApiStack.getApi().queryIntfImplementor(ILocker.class));
		}
		
		LockerValue v = (LockerValue) ext.newInstance("my_key");
		assertNotNull(v);
	}
}
