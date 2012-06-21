/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.tables;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.BusinessLogic;
import com.triniforce.db.dml.ISmartTran;
import com.triniforce.db.dml.ResSet;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IName;

/**
 * Table storing last generated key 
 */
public class NextId extends TableDef {
	
//	public static class PQGet extends PrepSql{
//		@Override
//		public QStatement buildSql() {
//			return new QSelect().joinLast(new SrvTable(NextId.class).addCol(value));
//		}
//		
//		public static Long exec(IStmtContainer sc){
//			Long res = null;
//			PrepStmt ps = sc.prepareStatement(PQGet.class);
//			ResSet rs = ps.executeQuery();
//			if(rs.next())
//				res =  rs.getLong(1);
//			return res;
//		} 
//	}
//
//    public static class PQClear extends PrepSql{
//		@Override
//		public QStatement buildSql() {
//			return new QDelete(new SrvTable(NextId.class));
//		}
//		
//		public static void exec(IStmtContainer sc){
//			PrepStmt ps = sc.prepareStatement(PQClear.class);
//			ps.execute();
//		} 
//	}
//
//    public static class PQSet extends PrepSql{
//		@Override
//		public QStatement buildSql() {
//			return new QInsert(new SrvTable(NextId.class).addCol(value));
//		}
//		
//		public static void exec(IStmtContainer sc, long v){
//			PrepStmt ps = sc.prepareStatement(PQSet.class);
//			ps.setLong(1, v);
//			ps.execute();
//		} 
//	}
    
//    public static final FieldDef value;// = FieldDef.createScalarField("value", ColumnType.LONG, true, 0L); //$NON-NLS-1$
//    static{
//    	value = FieldDef.createScalarField("value", ColumnType.LONG, true, 0L); //$NON-NLS-1$
//    }
    
    public static final String value = "value";
	
    public NextId() throws EDBObjectException {
        addField(1, FieldDef.createScalarField(value, ColumnType.LONG, true));
    }
    
    public NextIdBL getBL(ISmartTran sc){
		return new NextIdBL(sc, this);
    }
    
    public static class NextIdBL extends BusinessLogic{
		private NextId m_tab;

		public NextIdBL(ISmartTran sc, NextId tab) {
			setSt(sc);
			m_tab = tab;
		}

		@Override
		public Class getTable() {
			return m_tab.getClass();
		}
		
		public Long get(){
			ResSet rs = select(new IName[]{new ApiAlgs.SimpleName(value)}, new IName[]{}, new Object[]{});
			return rs.next() ? rs.getLong(1) : null;
		}
		
		public void clear(){
			delete(new IName[]{}, new Object[]{});
		}
		
		public void set(long v){
			insert(new IName[]{new ApiAlgs.SimpleName(value)}, new Object[]{v});
		}
		
    	
    }
    
}
