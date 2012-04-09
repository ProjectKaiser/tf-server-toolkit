/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.server.plugins.kernel.tables.EntityJournal;
import com.triniforce.server.plugins.kernel.tables.NextId;
import com.triniforce.server.plugins.kernel.tables.TNamedDbId;
import com.triniforce.server.srvapi.DataPreparationProcedure;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IServerMode;
import com.triniforce.server.srvapi.ISrvPrepSqlGetter;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IFiniter;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker.LockerValue;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap.IFactory;
import com.triniforce.server.srvapi.ISrvSmartTranFactory.ITranExtender;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IApi;
import com.triniforce.utils.IFinitable;
import com.triniforce.utils.IFinitableWithRollback;
import com.triniforce.utils.IProfiler;
import com.triniforce.utils.IProfilerStack;
import com.triniforce.utils.ITime;
import com.triniforce.utils.Profiler.INanoTimer;
import com.triniforce.utils.Profiler.ProfilerStack;

public class BasicServerCorePlugin implements IPlugin{
	
	private Api m_coreApi;
	private BasicServer m_basicSrv;
	private Api m_runningApi;
	private IdGenerator m_idGenerator;

	public BasicServerCorePlugin(Api coreApi, BasicServer basicSrv) {
		m_coreApi = coreApi;
		m_basicSrv = basicSrv;
	}

	public void doRegistration(ISORegistration reg) throws EDBObjectException {
        reg.registerTableDef(new EntityJournal<UpgradeProcedure>(BasicServer.UP_TABLE));
        reg.registerTableDef(new EntityJournal<DataPreparationProcedure>(BasicServer.DPP_TABLE));
        reg.registerTableDef(new NextId());
        reg.registerTableDef(new TNamedDbId());
        reg.registerTableDef(new com.triniforce.server.plugins.kernel.tables.TDbQueues());        
	}

	public String[] getDependencies() {
		return null;
	}

	public String getPluginName() {
		return null;
	}

	public String getProviderName() {
		return null;
	}


	public void prepareApi() {
        Api api = new Api();
        api.setIntfImplementor(ISOQuery.class, m_basicSrv);
        api.setIntfImplementor(ISODbInfo.class, m_basicSrv);

        /*if (m_idGenerator == null) {
            createIdGenerators();
        }*/

        //api.setIntfImplementor(IIdGenerator.class, m_idGenerator);

        api.setIntfImplementor(ISrvPrepSqlGetter.class,
                new SrvPrepSqlGetter());

        api.setIntfImplementor(IDbQueueFactory.class, new DbQueueFactory());

        api.setIntfImplementor(ISrvSmartTranFactory.class,
                m_tranFactory);
        
        ISOQuery soQuery = ApiStack.getInterface(ISOQuery.class);
        m_idGenerator = new IdGenerator(
        		IdGenerator.KEY_CACHE_SIZE,
                (NextId) soQuery.getEntity(NextId.class.getName()));
        api.setIntfImplementor(IIdGenerator.class, m_idGenerator);
        
        api.setIntfImplementor(IBasicServer.class, m_basicSrv);
        
        TNamedDbId namedIds = m_basicSrv.getEntity(TNamedDbId.class.getName());
        api.setIntfImplementor(INamedDbId.class, namedIds);
        
        m_runningApi = api;
        
        m_tranFactory.registerOuterExtender(new RefCountMapTrnExtender());
        m_tranFactory.registerOuterExtender(new FiniterExtender());
        m_tranFactory.registerOuterExtender(new LockerExtender());        
        
	}
	
	SrvSmartTranFactory m_tranFactory = new SrvSmartTranFactory();

	public void pushApi(final Mode mode, ApiStack apiStack) {
        if (mode.equals(Mode.Running) && m_runningApi == null)
            throw new BasicServer.EInvalidServerState(mode);

		
		Api api = new Api();
		api.setIntfImplementor(ISrvSmartTranFactory.class, m_tranFactory);
		api.setIntfImplementor(IServerMode.class, new IServerMode() {
            int m_stackSize = ApiStack.getThreadApiContainer().getStack()
                    .size();

            public Mode getMode() {
                return mode;
            }

            public int getStackSize() {
                return m_stackSize;
            }
        });
		IProfiler profiler = m_coreApi.getIntfImplementor(IProfiler.class);
        api.setIntfImplementor(IProfilerStack.class, new ProfilerStack(
                profiler, new INanoTimer() {
                    public long get() {
                        return System.nanoTime();
                    }
                }));
        
        switch (mode) {
        case Registration:
            api.setIntfImplementor(ISORegistration.class, m_basicSrv);
            api.setIntfImplementor(ISOQuery.class, m_basicSrv);
            api.setIntfImplementor(ITime.class, m_basicSrv);
            api.setIntfImplementor(ISrvSmartTranFactory.class, m_tranFactory);
            api.setIntfImplementor(IBasicServer.class, m_basicSrv);
            break;
        case Upgrade:
            api.setIntfImplementor(IIdGenerator.class, m_idGenerator);
            api.setIntfImplementor(IPooledConnection.class, m_coreApi
                    .getIntfImplementor(IPooledConnection.class));
            //api.setIntfImplementor(IIdGenerator.class, m_idGenerator);
            api.setIntfImplementor(ISOQuery.class, m_basicSrv);
            api.setIntfImplementor(ITime.class, m_basicSrv);
            api.setIntfImplementor(ISrvSmartTranFactory.class, m_tranFactory);
            api.setIntfImplementor(IBasicServer.class, m_basicSrv);
            break;
        case Running:
            break;
        }

		Stack<IApi> stk = apiStack.getStack();
		stk.push(m_coreApi);
		if(mode.equals(Mode.Running)){
			stk.push(m_runningApi);
		}
		stk.push(api);
	}

	public void popApi(Mode mode, ApiStack stk) {
	}
	
	static class RefCountMapTrnExtender implements ITranExtender{
		Map<Object, CountedObject> m_serverKeyMap = new HashMap<Object, CountedObject>(); 
		
		static class RefCountMap implements IRefCountHashMap{
			Map<Object, RefCountMapTrnExtender.CountedObject> m_map;
			Set<Object> m_transactionKeys = new HashSet<Object>();
			
			public RefCountMap(Map<Object, RefCountMapTrnExtender.CountedObject> map) {
				m_map = map;
			}
			public Object put(Object key, IFactory factory) {
				Object res;
				synchronized(m_map){
					if(getTransactionKeys().contains(key)){
						res = m_map.get(key).getObject();
						ApiAlgs.assertNotNull(res, "Keys corrupted");
					}
					else{
						RefCountMapTrnExtender.CountedObject countedObject = m_map.get(key); 
						if(null == countedObject){
							countedObject = new RefCountMapTrnExtender.CountedObject(factory.newInstance(key));
							m_map.put(key, countedObject);
						}
						m_transactionKeys.add(key);
						countedObject.addRef();
						res = countedObject.getObject(); 
					}
				}
				return res;
			}
			public Object getServerValue(Object key) {
				synchronized(m_map){
					RefCountMapTrnExtender.CountedObject countedObject = m_map.get(key); 
					return null == countedObject ? null : countedObject.getObject();
				}
			}
			public int getServerRefCount(Object key) {
				synchronized(m_map){
					RefCountMapTrnExtender.CountedObject countedObject = m_map.get(key); 
					return null == countedObject ? 0 : countedObject.getCounter();
				}
			}
			public Set<Object> getTransactionKeys() {
				return m_transactionKeys;
			}
			public Set<Object> getServerKeys() {
				return m_map.keySet();
			}
			public void flush() {
				synchronized(m_map){
					for (Object key : m_transactionKeys) {
						CountedObject countedObject = m_map.get(key);
						countedObject.release();
						if(countedObject.getCounter() == 0){
							m_map.remove(key);
						}
					}
					m_transactionKeys.clear();
				}
			}
		}
		
		static class CountedObject{
			int m_refNum;
			Object m_obj;
			CountedObject(Object obj){
				m_refNum = 0;
				m_obj = obj;
			}
			public Object getObject() {
				return m_obj;
			}
			public int getCounter() {
				return m_refNum;
			}
			void addRef(){
				++m_refNum;
			}
			void release(){
				--m_refNum;
			}
		}

		public void push() {
			Api api = new Api();
			api.setIntfImplementor(IRefCountHashMap.class, new RefCountMap(m_serverKeyMap));
			ApiStack.pushApi(api);
		}
		public void pop(boolean bCommit) {
			IApi topApi = ApiStack.getThreadApiContainer().getStack().pop();
			IRefCountHashMap refCounter = topApi.getIntfImplementor(IRefCountHashMap.class);
			refCounter.flush();
		}
	}
	
	static class FiniterExtender implements ITranExtender{

		static class Finiter implements IFiniter{

			private Set<IFinitable> m_finitables = new HashSet<IFinitable>();

			public void registerFiniter(IFinitable finiter) {
				m_finitables.add(finiter);
			}

			public Set<IFinitable> getRegisteredFiniters() {
				return m_finitables;
			}

			public void flush(boolean bCommit) {
				if(bCommit){
					for (IFinitable finitable: m_finitables) {
						try{
							finitable.finit();
						}catch(Throwable t){
							ApiAlgs.getLog(this).trace(t);
						}
					}
				}
				else{
					for (IFinitable finitable: m_finitables) {
						if(finitable instanceof IFinitableWithRollback)
							try{
								((IFinitableWithRollback)finitable).rollback();
							}catch(Throwable t){
								ApiAlgs.getLog(this).trace(t);
							}
					}
				}
			}
			
		}

		public void push() {
			Api api = new Api();
			api.setIntfImplementor(IFiniter.class, new Finiter());
			ApiStack.pushApi(api);
		}
		
		public void pop(boolean bCommit) {
			IApi api = ApiStack.getThreadApiContainer().getStack().pop();
			IFiniter finiter = api.getIntfImplementor(IFiniter.class);
			finiter.flush(bCommit);
		}
	}
	
	static class LockerExtender implements ITranExtender, IFactory{
	
		static class Locker implements ILocker{

			private IFactory m_factory;

			public Locker(IFactory factory) {
				m_factory = factory;
			}

			public void lock(ILockableObject key) {
				IRefCountHashMap countMap = ApiStack.getInterface(IRefCountHashMap.class);
                LockerKey lKey = new LockerKey(key);
				LockerValue lv = (LockerValue) countMap.put(lKey, m_factory);
				lv.lock(lKey);
				IFiniter finiter = ApiStack.getInterface(IFiniter.class);
				finiter.registerFiniter(lv);
			}
			
		}

		public void push() {
			Api api = new Api();
			api.setIntfImplementor(ILocker.class, new Locker(this));
			ApiStack.pushApi(api);
		}
		
		public void pop(boolean bCommit) {
			ApiStack.popApi();
		}

		public LockerValue newInstance(Object key) {
			return new LockerValue();
		}
		
	}

	public void finitApi() {
	
	}

}
