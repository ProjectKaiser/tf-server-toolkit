/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.dbo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.triniforce.server.plugins.kernel.BasicServer;
import com.triniforce.server.plugins.kernel.tables.EntityJournal;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISOQuery.EServerObjectNotFound;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class ExDBOAUpgradeProcedures extends DBOActualizer{
	
	public static String UP_TABLE = "com.triniforce.server.plugins.kernel.tables.UpgradeProcedures"; //$NON-NLS-1$

	public ExDBOAUpgradeProcedures() {
		super(true, Mode.Upgrade);
	}
	
	@Override
	public void actualize(List<IDBObject> dboList) {
		try{
			final List<UpgradeProcedure> upRegList = getUpgradeProcedures(dboList);
			if(upRegList.isEmpty())
				return ;
		
			BasicServer.runInSingleConnectionMode(new BasicServer.ICallback() {
		
				public void call() throws Exception {
					Connection connection = ApiStack.getApi()
							.getIntfImplementor(Connection.class);
					// upgrade Upgradeprocedures
					for (UpgradeProcedure proc : upRegList) {
		                ApiAlgs.getLog(this).info(
		                        String.format("Upgrade procedure: \"%s\"", proc.getEntityName())); //$NON-NLS-1$			    
						proc.run();
					}
					ISOQuery soQ   = ApiStack.getInterface(ISOQuery.class);
					ISODbInfo soDb = ApiStack.getInterface(ISODbInfo.class);

					((EntityJournal<UpgradeProcedure>) soQ.getEntity(UP_TABLE)).add(
							connection, soDb.getTableDbName(UP_TABLE), upRegList);
		
				}
			});
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}

	}

	private List<UpgradeProcedure> getUpgradeProcedures(List<IDBObject> dboList) throws EServerObjectNotFound, SQLException {
		ISOQuery soQ   = ApiStack.getInterface(ISOQuery.class);
		ISODbInfo soDb = ApiStack.getInterface(ISODbInfo.class);
		Connection con = ApiStack.getInterface(Connection.class);
		
		ArrayList<UpgradeProcedure> upList = new ArrayList<UpgradeProcedure>();
		for(IDBObject dbo : dboList){
			upList.add(((DBOUpgProcedure)dbo).getProc());
		}
		
		return ((EntityJournal<UpgradeProcedure>) soQ.getEntity(UP_TABLE))
		.exclude(con, soDb.getTableDbName(UP_TABLE), upList);
	}

}
