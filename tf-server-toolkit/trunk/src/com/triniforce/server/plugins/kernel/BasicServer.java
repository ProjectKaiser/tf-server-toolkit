/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.plugins.kernel;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import bsh.Interpreter;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.DBTables;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.EReferenceError;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.ddl.UpgradeRunner.IActualState;
import com.triniforce.dbo.DBOActualizer;
import com.triniforce.dbo.DBOSort;
import com.triniforce.dbo.DBOUpgProcedure;
import com.triniforce.dbo.IDBObject;
import com.triniforce.dbo.PKEPDBOActualizers;
import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.extensions.PKRootExtensionPoint;
import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor.BasicPeriodicalTask;
import com.triniforce.server.plugins.kernel.ep.srv_ev.PKEPServerEvents;
import com.triniforce.server.plugins.kernel.ep.srv_ev.ServerEvent;
import com.triniforce.server.plugins.kernel.tables.EntityJournal;
import com.triniforce.server.srvapi.DataPreparationProcedure;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IServerMode;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IEntity;
import com.triniforce.utils.IProfiler;
import com.triniforce.utils.ITime;
import com.triniforce.utils.Profiler;
import com.triniforce.utils.IProfilerStack.PSI;

/**
 * 
 * 
 */

/**
 * 
 * <h3>Constuctor BasicServer() + registrator</h3>
 * This variant is used to make sure that server invoke
 * <pre>
 *  bs = new BasicServer();
 *  bs.addPlugin(myPlugin1);
 *  bs.addPlugin(myPlugin2);
 *  bs.doPluginRegistration();
 *  bs.startRegistrator(Api baseApi, new Runnable(){...});
 *  </pre>

 * <p>Registrator first attempts to get working connection using IPooledConnection. If connection is failed it waits and makes next iteration.
 * <p>If connection is ok it does 
 * <pre>
 *  bs.setBaseApi(Api myApi);
 *  bs.doRegistration();
 *  </pre> 
 * If something goes wrong here error is caught and written into log with error level. 
 * <p>When server is registered Runnable argument is invoked (if not null). IBasicServer interface is in the ApiStack. 
 *  
 *  Possible synchronous processing:
 *  <pre>
 *  while(!bs.isRegistered()){
 *      ICheckInterrupted.Helper.sleep(1000);
 *  }  
 *  if( bs.isDbModificationNeeded()){
 *      bs.doDbModification()
 *  }
 * </pre>
 * As an option these actions can be made by startRegistrator() callback.
 * 
 * <h4>Registrator stoppingg</h4>
 * Registrator is stopped by stopAndWaitRegistrator(). Method works even if registrator has not been started.
 *  
 * <h3>Constuctor BasicServer(Api)</h3>
 * 
 * <pre>
 *  bs = new BasicServer(Api myApi);
 *  bs.doRegistration();
 *  if( bs.isDbModificationNeeded()){
 *      bs.doDbModification()
 *  }
 * </pre>
 * 
 * <h3>Constructor BasicServer()</h3>
 * 
 * <pre>
 *  bs = new BasicServer();
 *  bs.addPlugin(myPlugin1);
 *  bs.addPlugin(myPlugin2);
 *  bs.doPluginRegistration();
 *  bs.setBaseApi(Api myApi);
 *  bs.doRegistration();
 *  if( bs.isDbModificationNeeded()){
 *      bs.doDbModification()
 *  }
 * </pre>
 * 
 * <h3>Constructor BasicServer(Api api, plugins)</h3>
 * 
 * <pre>
 *  bs = new BasicServer(myApi, plugins);
 *  bs.doRegistration();
 *  if( bs.isDbModificationNeeded()){
 *      bs.doDbModification()
 *  }
 * </pre> 
 * <h3>General usage description</h3> Server creates its own core SrvApi based
 * on passed baseApi. The following interfaces are used:
 * <p>
 * 
 * <ul>
 * <li>ITime. Server installs this interface if it is not passed in baseApi
 * <li>ILogger. Must be installed in baseApi.
 * <li>IPooledConnection. Must be installed in baseApi.
 * </ul>
 * 
 * Actual plugin list is created from implicit plugin - ServerCorePlugin - and
 * passed plugin list. So ServerCorePlugin is the first plugin of the actual
 * plugin list
 * <p>
 * 
 * After creation user must call doRegistration method. This method enters
 * <tt>Registration mode</tt> and invokes do Registration() for each plugin from
 * the actual plugin list.
 * 
 * When doRegistration() is completed, isUpgradeNeeded() method returns true if
 * database upgrade needed. Upgrade is made by invoking doUpgrade(), which
 * enters <tt>Upgrade mode</tt>, created/update database objects and invokes
 * registered update procedures.
 * <p>
 * 
 * isDataPreparationNeeded() indicated that doDataPreparation() must be called.
 * doDataPreparation() enters <tt>Running mode</tt>, logs in as <tt>USystem</tt>
 * user and invokes all registered data preparation procedures.
 * <p>
 * 
 * All do*() methods installs core SrvApi in the beginning and restore previous
 * value before exit.
 * <p>
 * 
 * <h3>Connection issues</h3>
 * Before passing control to any plugin code server makes available database
 * connection through IConnection interface. E.g. it calls
 * IPooledConnection.getConnection() in the beginning of doRegistration() and
 * saves the connection for IConnection interface. At the end of
 * doRegistration() it calls IPooledConnection.returnConnection().
 * <p>
 * 
 * <h3>ServerCorePlugin issues</h3>
 * ServerCorePlugin.doRegistration() registers tables for the following
 * properties:
 * <p>
 * <ul>
 * <li>file-common-*-nonheritable
 * <li>dir-common-valueless-nonheritable
 * </ul>
 * mon Tables for another propertiy types are registered by server upon request.
 * <p>
 * <p>
 * 
 * @see ServerCorePlugin
 */

public class BasicServer extends PKRootExtensionPoint implements IBasicServer, ISORegistration, ISOQuery,
		ISODbInfo, ITime {

	public static class EInvalidServerState extends ServerException {
		private static final long serialVersionUID = -5675131699476728467L;

		private final Mode m_state;

		public EInvalidServerState(Mode state) {
			super(state.toString());
			m_state = state;
		}

		public Mode getState() {
			return m_state;
		}

	}

	public static class ServerException extends RuntimeException {
		private static final long serialVersionUID = -4159976254007224620L;

		public ServerException(String msg) {
			super(msg);
		}
	}

	public static String DPP_TABLE = "com.triniforce.server.plugins.kernel.tables.DataPrepProcedures"; //$NON-NLS-1$
	public static String UP_TABLE = "com.triniforce.server.plugins.kernel.tables.UpgradeProcedures"; //$NON-NLS-1$

	protected ApiStack m_coreApi;

	private List<IPlugin> m_plugins = new ArrayList<IPlugin>();;

	private boolean m_bRegistered;

	private boolean m_bDbModNeeded=true;

	private DBTables m_desiredTables;

	private IActualState m_actualTabStates;

	private LinkedHashMap<String, IEntity> m_entities;

//	private List<UpgradeProcedure> m_upRegList = null;

	private List<DataPreparationProcedure> m_dppRegList = null;
	
	TimeZone m_defTimeZone;

	public BasicServer() {
	    m_defTimeZone = TimeZone.getDefault();
	    TimeZone gmt = TimeZone.getTimeZone("GMT");
	    TimeZone.setDefault(gmt);
        addPlugin(new BasicServerCorePlugin());
	}
	

	public void setBaseApi(Api baseApi){
        Api serverApi = new Api();
        
        m_coreApi = new ApiStack();
        m_coreApi.pushApiIntoStack(baseApi);

        // install ITime interface
        if (null == baseApi.queryIntfImplementor(ITime.class)) {
            serverApi.setIntfImplementor(ITime.class, this);
        }
        // install IProfiler interface
        if (null == baseApi.queryIntfImplementor(IProfiler.class)) {
            serverApi.setIntfImplementor(IProfiler.class, new Profiler());
        }
        // install ISOQuery interface
        serverApi.setIntfImplementor(ISOQuery.class, this);

        serverApi.setIntfImplementor(IDatabaseInfo.class,
                getDbInfo((IPooledConnection) baseApi
                        .getIntfImplementor(IPooledConnection.class)));
        
        serverApi.setIntfImplementor(IBasicServer.class, this);
        serverApi.setIntfImplementor(IPKExtensionPoint.class, this);
        serverApi.setIntfImplementor(ITaskExecutors.class, m_ptExecutor.getTe());
        serverApi.setIntfImplementor(TimeZone.class, m_defTimeZone);
        
        m_coreApi.pushApiIntoStack(serverApi);      


        m_bRegistered = false;
//        m_bDbModNeeded = false;

        m_desiredTables = new DBTables();

        this.m_entities = new LinkedHashMap<String, IEntity>();

        // install IIdDef interface
        if (null == m_coreApi.queryIntfImplementor(IIdDef.class)) {
            serverApi
                    .setIntfImplementor(IIdDef.class,
                            new IdDef(ColumnType.LONG));
        }
	    
	}

	public BasicServer(Api baseApi) {
	    this();
	    setBaseApi(baseApi);
	}

	private IDatabaseInfo getDbInfo(IPooledConnection pool) {
		try {
			Connection conn = pool.getPooledConnection();
			try {
				final DbType dbType = UpgradeRunner.getDbType(conn);
				DatabaseMetaData md = conn.getMetaData();
				final String quoteString = md.getIdentifierQuoteString();
				return new IDatabaseInfo() {

					public DbType getDbType() {
						return dbType;
					}

					public String getIdentifierQuoteString() {
						return quoteString;
					}
				};
			} finally {
				pool.returnConnection(conn);
			}
		} catch (SQLException e) {
			ApiAlgs.rethrowException(e);
			return null;
		}
	}

	/**
	 * Create server. Server become not registered. doDbModification() - will
	 * throw exception
	 * 
	 * @param baseApi
	 *            base api of server. Must contains ILogger, IPooledConnection,
	 *            May contains ITime
	 * @param plugins
	 *            List of plugin. Will be registered in doRegistration()
	 *            procedure
	 */
	public BasicServer(Api baseApi, List<IPlugin> plugins) {

		this(baseApi);

		if (null != plugins) {
			addPlugins(plugins);
		}
		
		doPluginsRegistration();

	}

	boolean m_pluginRegistrationDone;
	
	public void doPluginsRegistration() {
	    if(m_pluginRegistrationDone) return;
        for (IPlugin plugin : m_plugins) {
            if (plugin instanceof PKPlugin) {
                ((PKPlugin) plugin).setRootExtensionPoint(this);
                getEpPlugins().putExtension(plugin);
            }
        }
        for (IPlugin plugin : m_plugins){
            if(plugin instanceof PKPlugin){
                ((PKPlugin)plugin).doExtensionPointsRegistration();
            }
        }
        for (IPlugin plugin : m_plugins){
            if(plugin instanceof PKPlugin){
                ((PKPlugin)plugin).doRegistration();
            }
        }
        ((PKEPServerEvents)getExtensionPoint(PKEPServerEvents.class)).handleEventDirectOrder(this, ServerEvent.PLUGINS_REGISTRATION_FINISHED);
        m_pluginRegistrationDone = true;
	}
	
	/**
	 * Register server. Run doRegistration for all plugin. Calculate
	 * isDbModification() flag
	 * 
	 * @throws ServerException
	 * @throws SQLException
	 * @throws FileDefException
	 * @throws EDBObjectException
	 */
	public void doRegistration() throws ServerException, SQLException,
			EDBObjectException {

		{// push IBasicServer api
			Api api = new Api();
			api.setIntfImplementor(IBasicServer.class, this);
			api.setIntfImplementor(IPKExtensionPoint.class,
					this);
			ApiStack.pushApi(api);
		}
        doPluginsRegistration();		
		try {

			if (isRegistered())
				throw new ServerException(Messages
						.getString("Server.SrvRegeisteredError")); //$NON-NLS-1$

			enterMode(Mode.Registration);
			try {

			    for (IPlugin plugin : m_plugins) {
                    plugin.doRegistration(this);
                }
			    
				for (IPlugin plugin : m_plugins)
					plugin.prepareApi();
				
				runActualizers(Mode.Registration);

				ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
				trf.push();
				try {
					Connection connection = ApiStack.getApi()
							.getIntfImplementor(Connection.class);

					// Get table states
					m_actualTabStates = new ActualStateBL(connection);
					m_desiredTables.setActualState(m_actualTabStates);
					UpgradeRunner player = new UpgradeRunner(connection,
							m_actualTabStates);
					player.init();
					connection.commit();

					m_bRegistered = true;

					if (!isDbModificationNeeded()) {

						loadRegLists(connection);

						m_bDbModNeeded = true; 
					}
				} finally {
					trf.pop();
				}

			} finally {
				leaveMode();
			}
		} finally {
			ApiStack.popApi();
		}
	}

	private void runActualizers(Mode mode) {
		Map<Class, List<IDBObject>> dboLists = getDBOLists();
		
		for(IPKExtension extension : getExtensionPoint(PKEPDBOActualizers.class).getExtensions().values()){
			DBOActualizer actualizer = extension.getInstance();
			if(mode.equals(actualizer.getAtualizationMode())){
				List<IDBObject> list = dboLists.get(actualizer.getClass());
				if(null == list)
					list = Collections.emptyList();
				if(!actualizer.hasOwnVersioning()){
					DBOSort sort = new DBOSort();
					list = sort.sort(list);
				}	
				actualizer.actualize(list);
			}
		}
	}


	/**
	 * Load registered upgrade procedures and data preparation procedures from
	 * database. Function called when tables upgraded
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	private void loadRegLists(Connection connection) throws SQLException {
		List<UpgradeProcedure> upList = getEntities(UpgradeProcedure.class);
		List<DataPreparationProcedure> dppList = new ArrayList<DataPreparationProcedure>();
		for (Iterator<UpgradeProcedure> iter = upList.iterator(); iter
				.hasNext();) {
			UpgradeProcedure up = iter.next();
			if (up instanceof DataPreparationProcedure) {
				dppList.add((DataPreparationProcedure) up);
				iter.remove();
			}
		}

//		m_upRegList = ((EntityJournal<UpgradeProcedure>) getEntity(UP_TABLE))
//				.exclude(connection, getTableDbName(UP_TABLE), upList);

		m_dppRegList = ((EntityJournal<DataPreparationProcedure>) getEntity(DPP_TABLE))
				.exclude(connection, getTableDbName(DPP_TABLE), dppList);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.triniforce.server.srvapi.ISORegistration#registerSelectFunction(com
	 * .triniforce.server.srvapi.SelectFunctionDef)
	 */
	// public void registerSelectFunction(SelectFunctionDef sfDef) {
	// registerEntity(sfDef);
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.triniforce.server.srvapi.ISORegistration#registerTableDef(com.triniforce
	 * .db.ddl.TableDef)
	 */
	public void registerTableDef(TableDef tableDef) {
		m_desiredTables.add(tableDef);
		registerEntity(tableDef);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.triniforce.server.srvapi.ISORegistration#registerUpgradeProcedure
	 * (com.triniforce.server.srvapi.UpgradeProcedure)
	 */
	public void registerUpgradeProcedure(UpgradeProcedure updProc) {
		registerEntity(updProc);
		
		IPKExtensionPoint ep = getExtensionPoint(PKEPDBObjects.class);
        ep.putExtension(updProc.getEntityName(), new DBOUpgProcedure(updProc));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.triniforce.server.srvapi.ISORegistration#registerDataPreparationProcedure
	 * (com.triniforce.server.srvapi.DataPreparationProcedure)
	 */
	public void registerDataPreparationProcedure(DataPreparationProcedure proc) {
		registerEntity(proc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.triniforce.server.srvapi.ISOQuery#getEntities(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public <T extends IEntity> List<T> getEntities(Class<T> cls) {
		if (cls == null)
			throw new IllegalArgumentException("cls"); //$NON-NLS-1$

		ArrayList<T> res = new ArrayList<T>();
		for (IEntity entity : m_entities.values()) {
			if (cls.isInstance(entity))
				res.add((T) entity);
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.triniforce.server.srvapi.ISOQuery#getEntity(java.lang.String)
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public <T extends IEntity> T getEntity(String entityName)
			throws EServerObjectNotFound {

		T res = (T) quieryEntity(entityName);
		if (res == null)
			throwESONotFound(entityName);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.triniforce.server.srvapi.ISOQuery#quieryEntity(java.lang.String)
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public <T extends IEntity> T quieryEntity(String entityName) {
		return (T) m_entities.get(entityName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.triniforce.server.srvapi.ISODbInfo#getTableDbName(java.lang.String)
	 */
	public String getTableDbName(String entityName)
			throws EServerObjectNotFound {
		String res = m_actualTabStates.getDBName(entityName);
		if (res == null)
			throwESONotFound(entityName);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.triniforce.server.srvapi.ITime#currentTimeMillis()
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * @return true if database modification needed
	 * @throws ServerException
	 *             if server is not registered
	 * @throws EReferenceError
	 *             something wrong in table referenes
	 */
	public boolean isDbModificationNeeded() throws ServerException,
			EReferenceError {
		if (!isRegistered())
			throw new ServerException(Messages
					.getString("Server.SrvNotRegError")); //$NON-NLS-1$
		
//		return true;
//
		if (!m_bDbModNeeded) {
			List<DBOperation> cl = m_desiredTables.getCommandList();
			m_bDbModNeeded = !cl.isEmpty();
		}
		return m_bDbModNeeded;
	}

	/**
	 * @return doRegistration are called
	 */
	public boolean isRegistered() {
		return m_bRegistered;
	}

	/**
	 * Run database modification first run table schemas modification
	 * 
	 * @throws EReferenceError
	 *             something wrong in desired table state
	 * @throws ServerException
	 * @throws SQLException
	 *             database error
	 * @throws ClassNotFoundException
	 */
	public void doDbModification() throws Exception {
		if (!isRegistered())
			throw new ServerException(Messages
					.getString("Server.SrvNotRegError")); //$NON-NLS-1$

		if (!isDbModificationNeeded())
			return;

		enterMode(Mode.Upgrade);
		try {
			ISrvSmartTranFactory trnFact = ApiStack.getApi()
					.getIntfImplementor(ISrvSmartTranFactory.class);
			trnFact.push();
			try {

				runInSingleConnectionMode(new ICallback() {

					public void call() throws Exception {
						Connection connection = ApiStack.getApi()
								.getIntfImplementor(Connection.class);

						// upgrade tables
						UpgradeRunner player = new UpgradeRunner(
								(Connection) connection, m_actualTabStates);
						List<DBOperation> cl = m_desiredTables.getCommandList();
						player.run(cl);
						
						connection.commit();
						runActualizers(Mode.Upgrade);
						

						// upgrade Upgradeprocedures
//						flushEntities(connection);

						connection.commit();
					}

				});
				
			} finally {
				trnFact.pop();
			}
		} finally {
			leaveMode();
		}

		m_bDbModNeeded = false;

		// run DataPreparationProcedures
		enterMode(Mode.Running);
		try {
			loadRegLists(ApiStack.getInterface(Connection.class));
			if (!m_dppRegList.isEmpty()) {
				runDataPreparationProcedures();
			}
		} finally {
			leaveMode();
		}
		
		enterMode(Mode.Running);
		try {
			runActualizers(Mode.Running);
		} finally {
			leaveMode();
		}
		
	}

	private Map<Class, List<IDBObject>> getDBOLists() {
		HashMap<Class, List<IDBObject>> res = new HashMap<Class, List<IDBObject>>();
		IPKExtensionPoint epDBO = getExtensionPoint(PKEPDBObjects.class);
		for(IPKExtension extension : epDBO.getExtensions().values()){
			IDBObject dbo = extension.getInstance();
			Class actKey = dbo.getActualizerClass();
			List<IDBObject> list = res.get(actKey);
			if(null == list){
				list = new ArrayList<IDBObject>();
				res.put(actKey, list);
			}
			list.add(dbo);
		}
		return res;
	}

	public interface ICallback {
		void call() throws Exception;
	}

	public static void runInSingleConnectionMode(ICallback cb) throws Exception {
		IDatabaseInfo dbInfo = ApiStack.getInterface(IDatabaseInfo.class);
		IPooledConnection pool = null;
		int oldMaxIdle = 0;
		if (DbType.FIREBIRD.equals(dbInfo.getDbType())) {
			pool = ApiStack.getInterface(IPooledConnection.class);
			oldMaxIdle = pool.getMaxIdle();
			pool.setMaxIdle(0);
		}
		try {
			cb.call();
		} finally {
			if (null != pool) {
				pool.setMaxIdle(oldMaxIdle);
			}
		}

	}

	protected void runDataPreparationProcedures() throws Exception {
		runInSingleConnectionMode(new ICallback() {
			public void call() throws Exception {
				Connection connection = ApiStack.getApi().getIntfImplementor(
						Connection.class);
				for (DataPreparationProcedure proc : m_dppRegList) {
					PSI psi = ApiAlgs.getProfItem("Server initialization", proc
							.getEntityName());
					try {
						ApiAlgs
								.getLog(this)
								.info(
										String
												.format(
														"Data preparation procedure: \"%s\"", proc.getEntityName())); //$NON-NLS-1$
						proc.run();
					} catch (Throwable t){
						ApiAlgs.getLog(this).error(t.getMessage(), t);
						ApiAlgs.rethrowException((Exception) t);
					} finally {
						ApiAlgs.closeProfItem(psi);
					}
				}
				((EntityJournal<DataPreparationProcedure>) getEntity(DPP_TABLE))
						.add(connection, getTableDbName(DPP_TABLE),
								m_dppRegList);
				SrvApiAlgs2.getIServerTran().commit();
			}
		});
	}

	/**
	 * Update registered files, properties and upgrade procedures (not data
	 * preparation proxedures)
	 * 
	 * @param connection
	 *            database connection
	 * @throws Exception
	 */
//	private void flushEntities(Connection connection) throws Exception {
//		if (m_upRegList == null) {
//			loadRegLists(connection);
//		}
//		if (!m_upRegList.isEmpty()) {
//			for (UpgradeProcedure proc : m_upRegList) {
//                ApiAlgs
//                .getLog(this)
//                .info(
//                        String
//                                .format(
//                                        "Upgrade procedure: \"%s\"", proc.getEntityName())); //$NON-NLS-1$			    
//				proc.run();
//			}
//			((EntityJournal<UpgradeProcedure>) getEntity(UP_TABLE)).add(
//					connection, getTableDbName(UP_TABLE), m_upRegList);
//		}
//	}

	/**
	 * All entity register procedures must call this
	 * 
	 * @param entity
	 */
	private void registerEntity(IEntity entity) {
		m_entities.put(entity.getEntityName(), entity);
	}

	/**
	 * Throw ESONotFound
	 * 
	 * @param objName
	 * @throws EServerObjectNotFound
	 */
	protected void throwESONotFound(String objName)
			throws EServerObjectNotFound {
		throw new EServerObjectNotFound(objName);
	}

	/**
	 * Switch server mode. For all modes server rigister Connection interface in
	 * his SrvApi
	 * 
	 * @param mode
	 *            Registration, Upgrade, Running (no None, call leaveMode)
	 * @return Old SrvApi, needed for leaveMode function
	 * @throws SQLException
	 */
	public void enterMode(final Mode mode) {
		if (mode.equals(Mode.None))
			throw new RuntimeException(Messages
					.getString("Server.LeaveModeError")); //$NON-NLS-1$

		if (mode.equals(Mode.Running) && m_bDbModNeeded)
			throw new BasicServer.EInvalidServerState(mode); //$NON-NLS-1$

		ApiStack newApiStack = new ApiStack();
		for (IPlugin plugin : m_plugins) {
			plugin.pushApi(mode, newApiStack);
		}
		ApiStack.pushApi(newApiStack);

		if (mode.equals(Mode.Running)) {
			ISrvSmartTranFactory trnFact = ApiStack.getApi()
					.getIntfImplementor(ISrvSmartTranFactory.class);
			trnFact.push();
		}

		/*
		 * if (mode.equals(Mode.Running)) { if (m_runningApi == null) throw new
		 * EInvalidServerState(mode); }
		 */
		/*
		 * 
		 * Api newApi = new Api();
		 * 
		 * { // create temporary Api
		 * 
		 * IProfiler profiler = m_coreApi.getIntfImplementor(IProfiler.class);
		 * newApi.setIntfImplementor(IProfilerStack.class, new ProfilerStack(
		 * profiler, new INanoTimer() { public long get() { return
		 * System.nanoTime(); } })); switch (mode) { case Registration:
		 * setRegistrationInterfaces(newApi); break; case Upgrade:
		 * setUpgradeInterfaces(newApi); break; case Running:
		 * setRunningInterfaces(newApi); break; } }
		 * 
		 * ApiStack newApiStack = new ApiStack();
		 * newApiStack.getStack().push(m_coreApi); // server core api if
		 * (mode.equals(Mode.Running)) {
		 * newApiStack.getStack().push(m_runningApi); // prepared running api }
		 * newApiStack.getStack().push(newApi); // individual mode temporary api
		 * 
		 * ApiStack.pushApi(newApiStack);
		 */
	}

	/**
	 * Leave mode
	 * 
	 */
	public void leaveMode() {

		IServerMode srvMode = ApiStack.getApi().getIntfImplementor(
				IServerMode.class);

		if (srvMode.getMode().equals(Mode.Running)) {
			ISrvSmartTran tr = ApiStack.queryInterface(ISrvSmartTran.class);
			if (null != tr) {

				ISrvSmartTranFactory trnFact = ApiStack.getApi()
						.getIntfImplementor(ISrvSmartTranFactory.class);
				trnFact.pop();
			}
		}

		try {

			int stSize = srvMode.getStackSize() + 1;

			if (stSize != ApiStack.getThreadApiContainer().getStack().size()) {
				ApiAlgs
						.getLog(this)
						.error(
								MessageFormat
										.format(
												"Api stack corrupted. Stack size: {0}, must be: {1}", ApiStack.getThreadApiContainer().getStack().size(), stSize));//$NON-NLS-1$
			}

		} catch (Throwable t) {
			ApiAlgs.getLog(this).error(
					"Error leaving mode " + srvMode.getMode(), t);//$NON-NLS-1$
		}
		// pop Api created in enterMode(). It must be last statement otherwise
		// ILogger is lost

		for (IPlugin plugin : m_plugins) {
			plugin.popApi(srvMode.getMode(), null);
		}

		ApiStack.popApi();

	}

	public void updateTableDef(TableDef oldDef, TableDef newDef) {
		m_desiredTables.add(newDef);
		try {
			Connection connection = ApiStack.getApi().getIntfImplementor(
					Connection.class);
			connection.commit();
			runInSingleConnectionMode(new ICallback() {
				public void call() throws Exception {
					// run operations and register/reregister entity
					Connection connection = ApiStack.getApi()
							.getIntfImplementor(Connection.class);
					UpgradeRunner player = new UpgradeRunner(
							(Connection) connection, m_actualTabStates);
					List<DBOperation> cl = m_desiredTables.getCommandList();
					player.run(cl);
				}
			});
			// register table again
			registerEntity(newDef);
		} catch (Exception e) {
			// rollback currentDef
			if (null == oldDef)
				m_desiredTables.remove(newDef.getEntityName());
			else
				m_desiredTables.add(oldDef);
			ApiAlgs.rethrowException(e);
		}
	}

	public void addPlugin(IPlugin plugin) {
		m_plugins.add(plugin);
	
	}

	public void addPlugins(List<IPlugin> plugins) {
		m_plugins.addAll(plugins);
	}

	public void initOrFinit(boolean isInit) {

		ListIterator<IPlugin> i;
		if (!isInit) {
			i = m_plugins.listIterator(m_plugins.size());
		} else {
			i = m_plugins.listIterator();
		}
		while (isInit ? i.hasNext() : i.hasPrevious()) {
			IPlugin plugin = isInit ? i.next() : i.previous();
			enterMode(Mode.Running);
			try {
				try {
					if (isInit) {
						plugin.init();
					} else {
						plugin.finit();
					}
					ISrvSmartTranFactory.Helper.commit();
				} catch (Throwable t) {
					String action = isInit ? "init" : "finit";
					ApiAlgs
							.getLog(this)
							.error(
									MessageFormat
											.format(
													"Plugin {0} {1} problem", plugin.getClass().getName(), action), t);//$NON-NLS-1$
				}
			} finally {
				leaveMode();
			}
		}
		if(isInit){
	        ((PKEPServerEvents)getExtensionPoint(PKEPServerEvents.class)).handleEventDirectOrder(this, ServerEvent.SERVER_INIT);		    
		} else{
		    ((PKEPServerEvents)getExtensionPoint(PKEPServerEvents.class)).handleEventReverseOrder(this, ServerEvent.SERVER_FINIT);		    
		}
	}

	public void finit() {
		initOrFinit(false);
	}

	public List<IPlugin> getPlugins() {
		return m_plugins;
	}

	List<BasicPeriodicalTask> m_tasks = new ArrayList<BasicPeriodicalTask>();

	public void addPeriodicalTask(BasicPeriodicalTask ptask) {
		m_tasks.add(ptask);
	}

	public List<BasicPeriodicalTask> getPeriodicalTasks() {
		return m_tasks;
	}

	PeriodicalTasksExecutor m_ptExecutor = new PeriodicalTasksExecutor();
	private RegistrationThread m_registratorThread;

	public void startPeriodicalTasks() {
		for (BasicPeriodicalTask task : m_tasks) {
			m_ptExecutor.scheduleWithFixedDelay(task, task.initialDelay,
					task.delay, task.unit);
		}
	}

	public void stopPeriodicalTasks() {
		m_ptExecutor.finit();
		m_ptExecutor = new PeriodicalTasksExecutor();
	}

	public void init() {
		initOrFinit(true);
	}

	public boolean executeBeanShell(File script) {
		return executeBeanShell(script, true);
	}

	public boolean executeBeanShell(File script, boolean enterMode) {
		if (!script.exists())
			return true;
		if (enterMode) {
			enterMode(Mode.Running);
		} else {
			ApiStack.pushApi(m_coreApi);
		}
		try {
			try {
				FileReader in = new FileReader(script);
				try {
					Interpreter intr = new Interpreter();
					intr.setStrictJava(true);
					intr.eval(in);
				} finally {
					in.close();
				}
				return true;
			} catch (Throwable t) {
				ApiAlgs.getLog(this).error(
						MessageFormat.format("Error executing {0}", script
								.getAbsolutePath()), t);
			}
		} finally {
			if (enterMode) {
				leaveMode();
			} else {
				ApiStack.popApi();
			}
		}
		return false;
	}

	public IActualState getActualDbState() {
		return m_actualTabStates;
	}

	public Set<String> getDbTableNames() {
		return ((ActualStateBL) m_actualTabStates).getDbTableNames();
	}

	public String getTableAppName(String dbName) throws EServerObjectNotFound {
		try {
			return m_actualTabStates.getAppName(dbName);
		} catch (SQLException e) {
			ApiAlgs.rethrowException(e);
			return null;
		}
	}

	public Set<String> getCompletedDataPreparationProcedures() {
		return getCompletedProcedures(DPP_TABLE);
	}

	public Set<String> getCompletedUpgradeProcedures() {
		return getCompletedProcedures(UP_TABLE);
	}

	private Set<String> getCompletedProcedures(String table) {
		try {
			return ((EntityJournal<UpgradeProcedure>) getEntity(table))
					.getActual(ApiStack.getInterface(Connection.class),
							getTableDbName(table));
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
			return null;
		}
	}

	public ApiStack getCoreApi() {
		return m_coreApi;
	}

	public void stopAndFinit(){
		try{
			stopPeriodicalTasks();
		}catch(Throwable t){
			ApiAlgs.getLog(this).error("Stop tasks error", t);
		}
		try{
			finit();
		}catch(Throwable t){
			ApiAlgs.getLog(this).error("Finit server error", t);
		}
	}

	public void initAndStart() {
		init();
		startPeriodicalTasks();
	}
	
	public static class RegistrationThread extends Thread{
		private static final long REGISTRATION_THREAD_TIMEOUT = 1000; //ms
		private static final long REGISTRATION_ERR_THREAD_TIMEOUT = 10000; //ms
		
		private BasicServer m_srv;
		private Api m_baseApi;
		private Runnable m_runnable;
		boolean bShutDown = false;
		
		public RegistrationThread(BasicServer srv, Api baseApi, Runnable runnable) {
			super(RegistrationThread.class.getSimpleName());
			m_srv = srv;
			m_baseApi = baseApi;
			m_runnable = runnable;
		}
		
		@Override
		synchronized public void run() {
			try {
				while(!bShutDown){
					IPooledConnection pool = m_baseApi.getIntfImplementor(IPooledConnection.class);
					Connection con;
					try {
						con = pool.getPooledConnection();
						try{
							con.getMetaData();
						}finally{
							pool.returnConnection(con);
						}
						try{
							m_srv.setBaseApi(m_baseApi);
							m_srv.doRegistration();
							ApiStack.pushInterface(IBasicServer.class, m_srv);
							try{
								m_runnable.run();
							} finally{
								ApiStack.popInterface(1);
							}
							return;
						}catch(Exception e){
							ApiAlgs.getLog(this).error(e.getMessage(), e);
							wait(REGISTRATION_ERR_THREAD_TIMEOUT);						
						}
					} catch (SQLException e) {
						ApiAlgs.getLog(this).trace("Server is not ready", e);
						wait(REGISTRATION_THREAD_TIMEOUT);
					}
				}
			} catch (InterruptedException e) {
				ApiAlgs.rethrowException(e);
			}
		}

		synchronized public void shutDown() {
			bShutDown = true;
			notify();
			try {
				join();
			} catch (InterruptedException e) {
				ApiAlgs.rethrowException(e);
			}
		}
	}
	
	public static class ERegistratorRunning extends RuntimeException{
		private static final long serialVersionUID = -9110194584550290752L;
	}


	public void startRegistrator(Api baseApi, Runnable runnable) {
		if(null != m_registratorThread)
			throw new ERegistratorRunning();
		try {
			m_registratorThread = new RegistrationThread(this, baseApi, runnable);
			m_registratorThread.start();
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}
	}
	
	synchronized public void stopAndWaitRegistrator(){
		if(null != m_registratorThread){
			m_registratorThread.shutDown();
			m_registratorThread = null;
		}
	}
	
	public void waitRegistrator(long millis){
		try {
			synchronized(m_registratorThread){
				m_registratorThread.wait(millis);
			}
		} catch (InterruptedException e) {
			ApiAlgs.rethrowException(e);
		}
	} 

}
