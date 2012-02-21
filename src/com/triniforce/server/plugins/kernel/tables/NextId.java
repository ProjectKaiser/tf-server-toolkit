/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.tables;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.IStmtContainer;
import com.triniforce.db.dml.PrepSql;
import com.triniforce.db.dml.PrepStmt;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.qbuilder.QDelete;
import com.triniforce.db.qbuilder.QInsert;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QStatement;
import com.triniforce.server.plugins.kernel.SrvTable;

/**
 * Table storing last generated key 
 */
public class NextId extends TableDef {
	
	public static class PQGet extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QSelect().joinLast(new SrvTable(NextId.class).addCol(value));
		}
		
		public static Long exec(IStmtContainer sc){
			Long res = null;
			PrepStmt ps = sc.prepareStatement(PQGet.class);
			ResSet rs = ps.executeQuery();
			if(rs.next())
				res =  rs.getLong(1);
			return res;
		} 
	}

    public static class PQClear extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QDelete(new SrvTable(NextId.class));
		}
		
		public static void exec(IStmtContainer sc){
			PrepStmt ps = sc.prepareStatement(PQClear.class);
			ps.execute();
		} 
	}

    public static class PQSet extends PrepSql{
		@Override
		public QStatement buildSql() {
			return new QInsert(new SrvTable(NextId.class).addCol(value));
		}
		
		public static void exec(IStmtContainer sc, long v){
			PrepStmt ps = sc.prepareStatement(PQSet.class);
			ps.setLong(1, v);
			ps.execute();
		} 
	}
    
    public static final FieldDef value;// = FieldDef.createScalarField("value", ColumnType.LONG, true, 0L); //$NON-NLS-1$
    static{
    	value = FieldDef.createScalarField("value", ColumnType.LONG, true, 0L); //$NON-NLS-1$
    }
	
    public NextId() throws EDBObjectException {
        addField(1, value);
    }
}
