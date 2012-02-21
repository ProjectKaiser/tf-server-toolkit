/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.tables;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.IStmtContainer;
import com.triniforce.db.dml.PrepSql;
import com.triniforce.db.dml.PrepStmt;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.dml.StmtContainer;
import com.triniforce.db.qbuilder.QDelete;
import com.triniforce.db.qbuilder.QInsert;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QStatement;
import com.triniforce.db.qbuilder.WhereClause;
import com.triniforce.server.plugins.kernel.SrvTable;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;

public class TNamedDbId extends TableDef implements INamedDbId {
	
	public static final FieldDef id = FieldDef.createScalarField("ID", ColumnType.LONG, true);
	public static final FieldDef name = FieldDef.createStringField("name", ColumnType.VARCHAR, 250, true, null);
	
	static class EInterfaceNotLoaded extends RuntimeException{
		private static final long serialVersionUID = 6125067000647690657L;
	}
	
	Map<String, Long> m_idByName;
	Map<Long, String> m_nameById;
	
	public TNamedDbId() {
		addField(1, id);
		addField(2, name);
		addPrimaryKey(3, "pk",  new String[]{id.getName()});
		addIndex(4, "idx_name", new String[]{name.getName()}, true, true);
	}
	
	public static class PQInsert extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QInsert(new SrvTable(TNamedDbId.class).addCol(id).addCol(name));
		}
		
		public static void exec(IStmtContainer sc, long id, String name){
			PrepStmt ps = sc.prepareStatement(PQInsert.class);
			ps.setLong(1, id);
			ps.setObject(2, name);
			ps.execute();
		}
	}
	
	public static class PQGetByName extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QSelect()
			.joinLast(new SrvTable(TNamedDbId.class).addCol(id))
			.where(new WhereClause().andCompare("", name.getName(), "="));
		}
		
		public static Long exec(String name){
			StmtContainer sc = SrvApiAlgs2.getStmtContainer();
			try{
				PrepStmt ps = sc.prepareStatement(PQGetByName.class);
				ps.setObject(1, name);
				ResSet rs = ps.executeQuery();
				if(rs.next()){
					return rs.getLong(1);
				}
				return null;
			}finally{
				sc.close();
			}
			
		}
	}
	
	public static class PQGetAll extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QSelect()
			.joinLast(new SrvTable(TNamedDbId.class).addCol(id).addCol(name));
		}
		
		public static Map<String, Long> exec(){
			HashMap<String, Long> res = new HashMap<String, Long>();
			StmtContainer sc = SrvApiAlgs2.getStmtContainer();
			try{
				PrepStmt ps = sc.prepareStatement(PQGetAll.class);
				ResSet rs = ps.executeQuery();
				while(rs.next()){
					res.put(rs.getString(2), rs.getLong(1));
				}
				return res;
			}finally{
				sc.close();
			}
		}		
	}
	
	public static class PQGetById extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QSelect()
			.joinLast(new SrvTable(TNamedDbId.class).addCol(name))
			.where(new WhereClause().andCompare("", id.getName(), "="));
		}
		
		public static String exec(long id){
			StmtContainer sc = SrvApiAlgs2.getStmtContainer();
			try{
				PrepStmt ps = sc.prepareStatement(PQGetById.class);
				ps.setObject(1, id);
				ResSet rs = ps.executeQuery();
				if(rs.next()){
					return rs.getString(1);
				}
				return null;
			}finally{
				sc.close();
			}
			
		}
	}
	
	public static class PQDelete extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QDelete(new SrvTable(TNamedDbId.class))
			.where(new WhereClause().andCompare("", id.getName(), "="));
		}
		
		public static void exec(IStmtContainer sc, long id){
			PrepStmt ps = sc.prepareStatement(PQDelete.class);
			ps.setLong(1, id);
			ps.execute();
		}
	}
	
	interface ICallback{
		Object call(ISrvSmartTran trn);
	}
	
	Object callInOwntransaction(ICallback cb){
		ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
		trf.push();
		try{
			ISrvSmartTran trn = SrvApiAlgs2.getIServerTran();
			Object res = cb.call(trn);
			trn.commit();
			return res;
		} finally{
			trf.pop();
		}
	}

	public synchronized long createId(final String name) {
		checkLoaded();
		{
		    Long res = m_idByName.get(name);
		    if( null != res) return res;
		}
		
		return (Long)callInOwntransaction(new ICallback() {
			public Object call(ISrvSmartTran trn) {
				Long res; 
				res = PQGetByName.exec(name);
				if(null == res){
					res = SrvApiAlgs2.generateId();
					PQInsert.exec(trn, res, name);
					m_idByName.put(name, res);
					m_nameById.put(res, name);
				}
				return res;
			}});
	}

	public synchronized void dropId(final long id) {
		checkLoaded();
		callInOwntransaction(new ICallback() {
			public Object call(ISrvSmartTran trn) {
				PQDelete.exec(trn, id);
				String vname = m_nameById.remove(id);
				m_idByName.remove(vname);
				return null;
			}
		});
	}

	public Long queryId(String name) {
		checkLoaded();
		return m_idByName.get(name);
//		return PQGetByName.exec(name);
	}
	
	public long getId(String name) throws ENotFound {
		Long res = queryId(name);
		if(null == res)
			throw new ENotFound(name);
		return res.longValue();
	}

	public String getName(long id) throws ENotFound {
		checkLoaded();
		String res = m_nameById.get(id);
//		String res = PQGetById.exec(id);
		if(null == res){
			throw new ENotFound(id);
		}
		return res;
	}
	
	public static class PQDeleteAll extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QDelete(new SrvTable(TNamedDbId.class));
		}
		
		public static void exec(IStmtContainer sc){
			PrepStmt ps = sc.prepareStatement(PQDeleteAll.class);
			ps.execute();			
		}
	}
	
	/**
	 * For tests
	 */
	public void clear(){
		checkLoaded();
		callInOwntransaction(new ICallback() {
			public Object call(ISrvSmartTran trn) {
				PQDeleteAll.exec(trn);
				return null;
			}});
	}

	private void checkLoaded() {
		if(null == m_idByName)
			loadData();
	}
	
	public void loadData(){
		m_idByName = PQGetAll.exec();
		m_nameById = new HashMap<Long, String>();
		for (Map.Entry<String, Long> entry : m_idByName.entrySet()) {
			m_nameById.put(entry.getValue(), entry.getKey());
		}
	}


}
