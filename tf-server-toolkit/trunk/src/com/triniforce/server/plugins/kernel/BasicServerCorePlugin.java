/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.dbo.DBOUpgProcedure;
import com.triniforce.dbo.DBOVersion;
import com.triniforce.dbo.ExDBOATables;
import com.triniforce.dbo.ExDBOAUpgradeProcedures;
import com.triniforce.dbo.PKEPDBOActualizers;
import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.qsync.impl.DboQSyncActualizer;
import com.triniforce.qsync.impl.QSyncExternals;
import com.triniforce.qsync.impl.QSyncManager;
import com.triniforce.qsync.impl.QSyncManagerTask;
import com.triniforce.qsync.impl.TQSyncQueues;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.server.TFPlugin;
import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIs;
import com.triniforce.server.plugins.kernel.ep.br.PKEPBackupRestore;
import com.triniforce.server.plugins.kernel.ep.external_classes.ExternalClasses;
import com.triniforce.server.plugins.kernel.ep.external_classes.PKEPExternalClasses;
import com.triniforce.server.plugins.kernel.ep.sp.PKEPServerProcedures;
import com.triniforce.server.plugins.kernel.ep.srv_ev.PKEPServerEvents;
import com.triniforce.server.plugins.kernel.ep.tr_ext.PKEPTranInners;
import com.triniforce.server.plugins.kernel.ep.tr_ext.PKEPTranOuters;
import com.triniforce.server.plugins.kernel.ext.api.Mailer;
import com.triniforce.server.plugins.kernel.ext.api.PTRecurringTasks;
import com.triniforce.server.plugins.kernel.ext.br.BackupRestoreDb;
import com.triniforce.server.plugins.kernel.ext.br.BackupRestorePluginVersions;
import com.triniforce.server.plugins.kernel.recurring.PKEPRecurringTasks;
import com.triniforce.server.plugins.kernel.recurring.TRecurringTasks;
import com.triniforce.server.plugins.kernel.services.PKEPServices;
import com.triniforce.server.plugins.kernel.tables.EntityJournal;
import com.triniforce.server.plugins.kernel.tables.NextId;
import com.triniforce.server.plugins.kernel.tables.TNamedDbId;
import com.triniforce.server.plugins.kernel.tables.TQueueId;
import com.triniforce.server.plugins.kernel.upg_procedures.BreakIdGenerator;
import com.triniforce.server.plugins.kernel.upg_procedures.ConvertForeignKeys;
import com.triniforce.server.plugins.kernel.upg_procedures.Upg_120406_NamedDbIdNname;
import com.triniforce.server.srvapi.DataPreparationProcedure;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.IMiscIdGenerator;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IServerMode;
import com.triniforce.server.srvapi.ISrvPrepSqlGetter;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IFiniter;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker.LockerValue;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap.IFactory;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.ISrvSmartTranFactory.ITranExtender;
import com.triniforce.server.srvapi.ITimedLock2;
import com.triniforce.server.srvapi.ITimedLock2.ITimedLockCB;
import com.triniforce.server.srvapi.ITransactionWriteLock2;
import com.triniforce.server.srvapi.UpgradeProcedure;
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
import com.triniforce.utils.TFUtils;

public class BasicServerCorePlugin extends TFPlugin implements IPlugin{
	
	private Api m_runningApi;
	private IdGenerator m_idGenerator;
	private MiscIdGenerator m_miscIdGenerator;
	private TQueueId m_tQueueId;

	private BasicServer getBasicServer(){
	    return (BasicServer) getRootExtensionPoint();
	}
	
	@Override
	public String getVersion() {
	    return "5.3";
	}
	
	public BasicServerCorePlugin() {
		m_tQueueId =  new TQueueId();
        m_miscIdGenerator = new MiscIdGenerator(
        		IdGenerator.KEY_CACHE_SIZE,
                m_tQueueId);
	}

	public void doRegistration(ISORegistration reg) throws EDBObjectException {
        reg.registerTableDef(new EntityJournal<UpgradeProcedure>(BasicServer.UP_TABLE));
        reg.registerTableDef(new EntityJournal<DataPreparationProcedure>(BasicServer.DPP_TABLE));
        reg.registerTableDef(new NextId());
        reg.registerTableDef(new TNamedDbId());
        reg.registerTableDef(new com.triniforce.server.plugins.kernel.tables.TDbQueues());
        reg.registerTableDef(m_tQueueId);
        reg.registerTableDef(new TRecurringTasks());
        
        reg.registerUpgradeProcedure(new Upg_120406_NamedDbIdNname());
        
        putExtension(PKEPDBObjects.class, ConvertForeignKeys.class.getName(), 
        		new DBOUpgProcedure(new ConvertForeignKeys()));
        putExtension(PKEPDBObjects.class, BreakIdGenerator.class.getName() + ".queue", 
        		new DBOUpgProcedure(new BreakIdGenerator(m_miscIdGenerator)));
        
        putExtension(PKEPDBObjects.class, DBOVersion.class.getName(), new DBOVersion());
     
	}
	
	public void doRegistration(){
    
	    putExtension(PKEPTranOuters.class, RefCountMapTrnExtender.class);
	    putExtension(PKEPTranOuters.class, FiniterExtender.class);
	    putExtension(PKEPTranOuters.class, LockerExtender.class);
	    putExtension(PKEPTranOuters.class, TransactionWriteLockExtender.class);
	    
        putExtension(PKEPBackupRestore.class, BackupRestoreDb.class);
        putExtension(PKEPBackupRestore.class, BackupRestorePluginVersions.class);
        
        putExtension(PKEPAPIs.class, ThrdWatcherRegistrator.class);
        putExtension(PKEPAPIs.class, TimedLock2.class);
        
        putExtension(PKEPAPIs.class, ExternalClasses.class);
        
        putExtension(PKEPAPIs.class, PTRecurringTasks.class);
        putExtension(PKEPAPIs.class, Mailer.class);

		putExtension(PKEPDBOActualizers.class, DboQSyncActualizer.class);
		putExtension(PKEPDBObjects.class, TQSyncQueues.class);

		putExtension(PKEPAPIs.class, QSyncManagerTask.class);

	}

	public String[] getDependencies() {
		return null;
	}

	public String getPluginName() {
		return "Core Server Functionality";
	}

	/* (non-Javadoc)
	 * @see com.triniforce.extensions.PKPlugin#prepareApi()
	 */
	public void prepareApi() {
        Api api = new Api();
        api.setIntfImplementor(ISOQuery.class, getBasicServer());
        api.setIntfImplementor(ISODbInfo.class, getBasicServer());

        /*if (m_idGenerator == null) {
            createIdGenerators();
        }*/

        //api.setIntfImplementor(IIdGenerator.class, m_idGenerator);

        api.setIntfImplementor(ISrvPrepSqlGetter.class,
                new SrvPrepSqlGetter());

        ISOQuery soQuery = ApiStack.getInterface(ISOQuery.class);
        api.setIntfImplementor(IDbQueueFactory.class, new DbQueueFactory(m_miscIdGenerator));

        api.setIntfImplementor(ISrvSmartTranFactory.class,
                m_tranFactory);
        
        m_idGenerator = new IdGenerator(
        		IdGenerator.KEY_CACHE_SIZE,
                (NextId) soQuery.getEntity(NextId.class.getName()));
        api.setIntfImplementor(IIdGenerator.class, m_idGenerator);
        api.setIntfImplementor(IMiscIdGenerator.class, m_miscIdGenerator);
        
        api.setIntfImplementor(IBasicServer.class, getBasicServer());
        api.setIntfImplementor(IPKExtensionPoint.class, getBasicServer());
        
        TNamedDbId namedIds = getBasicServer().getEntity(TNamedDbId.class.getName());
        api.setIntfImplementor(INamedDbId.class, namedIds);

        //Sync manager
        QSyncManager sMan = new QSyncManager();
        sMan.setSyncerExternals(new QSyncExternals());
        api.setIntfImplementor(IQSyncManager.class, sMan);
        
        m_runningApi = api;

        // instantiate APIs
        {
            IPKExtensionPoint ep = getRootExtensionPoint().getExtensionPoint(PKEPAPIs.class);
            for(IPKExtension e:ep.getExtensions().values()){
                IPKEPAPI epApi = e.getInstance();
                if(null != epApi.getImplementedInterface()){
                    m_runningApi.setIntfImplementor(epApi.getImplementedInterface(), epApi);
                }
            }
        }

        //LATER: почему здесь а не в doRegistration() ?
        //tran inners
        {
            IPKExtensionPoint ep = getRootExtensionPoint().getExtensionPoint(PKEPTranInners.class);
            for( IPKExtension e : ep.getExtensions().values()){
                m_tranFactory.registerInnerExtender((ITranExtender) e.getInstance());
            }
        }
        //tran outers
        {
            IPKExtensionPoint ep = getRootExtensionPoint().getExtensionPoint(PKEPTranOuters.class);
            for( IPKExtension e : ep.getExtensions().values()){
                m_tranFactory.registerOuterExtender((ITranExtender) e.getInstance());
            }            
        }
        
        
        
	}
	
	SrvSmartTranFactory m_tranFactory = new SrvSmartTranFactory();

	public void pushApi(final Mode mode, ApiStack apiStack) {
	    ApiStack coreApi = getBasicServer().getCoreApi();
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
		IProfiler profiler = coreApi.getIntfImplementor(IProfiler.class);
        api.setIntfImplementor(IProfilerStack.class, new ProfilerStack(
                profiler, new INanoTimer() {
                    public long get() {
                        return System.nanoTime();
                    }
                }));
        
        switch (mode) {
        case Registration:
            api.setIntfImplementor(ISORegistration.class, getBasicServer());
            api.setIntfImplementor(ISOQuery.class, getBasicServer());
            api.setIntfImplementor(ITime.class, getBasicServer());
            api.setIntfImplementor(ISrvSmartTranFactory.class, m_tranFactory);
            api.setIntfImplementor(IBasicServer.class, getBasicServer());
            api.setIntfImplementor(IPKExtensionPoint.class, getBasicServer());
            break;
        case Upgrade:
            api.setIntfImplementor(IIdGenerator.class, m_idGenerator);
            api.setIntfImplementor(IPooledConnection.class, coreApi
                    .getIntfImplementor(IPooledConnection.class));
            //api.setIntfImplementor(IIdGenerator.class, m_idGenerator);
            api.setIntfImplementor(ISOQuery.class, getBasicServer());
            api.setIntfImplementor(ITime.class, getBasicServer());
            api.setIntfImplementor(ISrvSmartTranFactory.class, m_tranFactory);
            api.setIntfImplementor(IBasicServer.class, getBasicServer());
            api.setIntfImplementor(IPKExtensionPoint.class, getBasicServer());
            api.setIntfImplementor(ISODbInfo.class, getBasicServer());
            TNamedDbId namedIds = getBasicServer().getEntity(TNamedDbId.class.getName());
            api.setIntfImplementor(INamedDbId.class, namedIds);

            api.setIntfImplementor(ISrvPrepSqlGetter.class,
                    m_runningApi.getIntfImplementor(ISrvPrepSqlGetter.class));

            api.setIntfImplementor(IDbQueueFactory.class, 
            		m_runningApi.getIntfImplementor(IDbQueueFactory.class));

            break;
        case Running:
            break;
        }

		Stack<IApi> stk = apiStack.getStack();
		stk.push(coreApi);
		if(mode.equals(Mode.Running)){
			stk.push(m_runningApi);
		}
		stk.push(api);
	}

	public void popApi(Mode mode, ApiStack stk) {
	}
	
	public static class RefCountMapTrnExtender implements ITranExtender{
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
						TFUtils.assertNotNull(res, "Keys corrupted");
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
	
	public static class FiniterExtender implements ITranExtender{

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
	
	public static class LockerExtender implements ITranExtender, IFactory{
	
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

	public void finit() {
		
	}

	public void init() {
	}
	
    static class TransactionWriteLock implements ITransactionWriteLock2, ITimedLock2.ITimedLockCB {

        ISrvSmartTran m_tran;
        private List<ITimedLockCB> m_cb = new ArrayList<ITimedLockCB>();        
        
        /**
         * Multiple calls allowed. Each cb will be called back.
         */
        public void lock(ITimedLockCB cb) {
            ITimedLock2 tl = ApiStack.queryInterface(ITimedLock2.class);

            if(null == tl) return;//registration phase
            
            if(null != cb){
                m_cb.add(cb);
            }
            m_tran = ApiStack.getInterface(ISrvSmartTran.class);;            
            tl.acquireLock(this);
        }
        public void unlock() {
            ITimedLock2 tl = ApiStack.queryInterface(ITimedLock2.class);
            
            if(null == tl) return;//registration phase            
            
            tl.releaseLock(this);
            for(ITimedLockCB cb: m_cb){
                cb.unlocked();
            }
        }        
        public void unlocked() {
            if( null != m_tran){
                m_tran.doNotCommit();
            }
        }
        public void lock() {
            lock(null);
        }
    }

    public static class TransactionWriteLockExtender implements ITranExtender {

        public void pop(boolean arg0) {
            ITransactionWriteLock2 wl = ApiStack
                    .getInterface(ITransactionWriteLock2.class);
            wl.unlock();

            ApiStack.popApi();
        }
        public void push() {
            Api api = new Api();
            api.setIntfImplementor(ITransactionWriteLock2.class,
                    new TransactionWriteLock());           
            ApiStack.pushApi(api);
        }       
    }
    
    public void doExtensionPointsRegistration() {
        putExtensionPoint(new PKEPServerProcedures());
        putExtensionPoint(new PKEPBackupRestore());
        putExtensionPoint(new PKEPServerEvents());
        putExtensionPoint(new PKEPRecurringTasks());
        PKEPDBOActualizers actualizers = new PKEPDBOActualizers();
        actualizers.putExtension(ExDBOATables.class);
        actualizers.putExtension(ExDBOAUpgradeProcedures.class);
        putExtensionPoint(actualizers);
        putExtensionPoint(new PKEPDBObjects());
        putExtensionPoint(new PKEPServices());
        putExtensionPoint(new PKEPTranInners());
        putExtensionPoint(new PKEPTranOuters());
        putExtensionPoint(new PKEPAPIs());
        
        putExtensionPoint(new PKEPExternalClasses());
        

    }


}
