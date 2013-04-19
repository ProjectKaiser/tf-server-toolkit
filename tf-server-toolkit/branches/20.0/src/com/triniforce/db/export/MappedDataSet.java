/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.CompositeTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.ActualStateBL.TIndexNames;
import com.triniforce.utils.ApiAlgs;

public class MappedDataSet extends AbstractDataSet {
	
	private IDataSet m_archive;
	private Map<String, String> m_realAppDbNameMap;

	public MappedDataSet(Map<String, String> realAppDbNameMap, IDataSet archive) {
		m_archive = archive;
		m_realAppDbNameMap = realAppDbNameMap;
	}
	
	HashSet<String> UNEXPORTED_TABS = new HashSet<String>(
			Arrays.asList(ActualStateBL.ACT_STATE_TABLE, TIndexNames.class.getName())); 

	@Override
	protected ITableIterator createIterator(boolean reversed)
			throws DataSetException {
		ArrayList<ITable> tabs = new ArrayList<ITable>();
		Map<String,String> archiveDbAppMap = getAppNames(m_archive);
		for(String archiveDbName : m_archive.getTableNames()){
			archiveDbName = archiveDbName.toLowerCase();
			String appName = archiveDbAppMap.get(archiveDbName);
			if(null == appName){
			    ApiAlgs.getLog(this).warn("Empty appName for " + archiveDbName);
			    continue;
			}
			if(!UNEXPORTED_TABS.contains(appName)){
				ITable db1Tab = m_archive.getTable(archiveDbName);
				String db2Name = m_realAppDbNameMap.get(appName);
				if(null == db2Name){
	                ApiAlgs.getLog(this).warn("Table " + appName + " doesn't exist");
	                continue;
				}
				CompositeTable db2Tab = new CompositeTable(db2Name, db1Tab);
				tabs.add(db2Tab);
			}
		}
		return new DefaultTableIterator(tabs.toArray(new ITable[tabs.size()]), reversed);
	}
	
	private Map<String, String> getAppNames(IDataSet src) {
		try {
			HashMap<String, String> res = new HashMap<String, String>();
			ITable asTab = src.getTable(ActualStateBL.ACT_STATE_TABLE);
			for(int i=0; i<asTab.getRowCount(); i++){
				res.put((String) asTab.getValue(i, ActualStateBL.DBNAME), 
						(String) asTab.getValue(i, ActualStateBL.APPNAME));
			}
			return res;
		} catch (DataSetException e) {
			ApiAlgs.rethrowException(e);
			return null;
		}
	}

	Collection<String> getAppTables(){
		return m_realAppDbNameMap.keySet();
	}

}
