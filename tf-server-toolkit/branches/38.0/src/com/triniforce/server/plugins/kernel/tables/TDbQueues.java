/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 

package com.triniforce.server.plugins.kernel.tables;

import java.io.InputStream;
import java.sql.SQLException;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.dml.IStmtContainer;
import com.triniforce.db.dml.PrepSql;
import com.triniforce.db.dml.PrepStmt;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.qbuilder.OrderByClause;
import com.triniforce.db.qbuilder.QDelete;
import com.triniforce.db.qbuilder.QInsert;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QStatement;
import com.triniforce.db.qbuilder.WhereClause;
import com.triniforce.server.plugins.kernel.SrvTable;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiAlgs;

public class TDbQueues extends TableDef {
	
//    public static final FieldDef id = FieldDef.createScalarField(
//            "id", FieldDef.ColumnType.LONG, true);  //$NON-NLS-1$
    public static final FieldDef id = IIdDef.Helper.getFieldDef("id");
    public static final FieldDef queueId = IIdDef.Helper.getFieldDef("queue_id");
//    FieldDef.createScalarField(
//            "queue_id", FieldDef.ColumnType.LONG, true);  //$NON-NLS-1$
    public static final FieldDef data = FieldDef.createScalarField(
            "data", FieldDef.ColumnType.BLOB, true); //$NON-NLS-1$

    public static class PQInsert extends PrepSql{
        @Override
        public QStatement buildSql() {
            return new QInsert(new SrvTable(TDbQueues.class)
            .addCol(TDbQueues.id)
            .addCol(TDbQueues.queueId)
            .addCol(TDbQueues.data));
        }
        
        public static void exec(long id, long queueId, InputStream data, int dataSz){
            IStmtContainer sc = SrvApiAlgs2.getStmtContainer();
            try {
                PrepStmt ps = sc.prepareStatement(TDbQueues.PQInsert.class);
                ps.setLong(1, id);
                ps.setLong(2, queueId);
                ps.setBinaryStream(3, data, dataSz);
                ps.execute();
            } finally {
                sc.close();
            }
        }
    }
    
    public static class PQGetHead extends PrepSql{
    	@Override
    	public QStatement buildSql() {
    		return new QSelect().joinLast(new SrvTable(TDbQueues.class)
            .addCol(TDbQueues.id)
            .addCol(TDbQueues.data))
            .where(new WhereClause().andCompare("", TDbQueues.queueId.getName(), "=")) //$NON-NLS-1$ //$NON-NLS-2$
            .orderBy(new OrderByClause().addCol("", TDbQueues.id.getName())); //$NON-NLS-1$
    	}
    	
        public static ResSet exec(IStmtContainer sc, long queueId){
            PrepStmt ps = sc.prepareStatement(TDbQueues.PQGetHead.class);
            try {
				ps.getStatement().setFetchSize(1);
			} catch (SQLException e) {
				ApiAlgs.rethrowException(e);
			}
            ps.setLong(1, queueId);
            return ps.executeQuery();
        }
    }
    
    public static class PQDelete extends PrepSql {
    	@Override
    	public QStatement buildSql() {
    		return new QDelete(new SrvTable(TDbQueues.class))
    		.where(new WhereClause()
            .andCompare("", TDbQueues.queueId.getName(), "=")
            .andCompare("", TDbQueues.id.getName(), "=")); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	
    	public static void exec(long queueId, long id){
            IStmtContainer sc = SrvApiAlgs2.getStmtContainer();
            try {
                exec(sc, queueId, id);
            } finally {
                sc.close();
            }   		
    	}

        public static void exec(IStmtContainer sc, long queueId, long id) {
            PrepStmt ps = sc.prepareStatement(TDbQueues.PQDelete.class);
            ps.setLong(1, queueId);
            ps.setLong(2, id);
            ps.execute();
        }
    }
    
    public static class PQDeleteAll extends PrepSql {
        @Override
        public QStatement buildSql() {
            return new QDelete(new SrvTable(TDbQueues.class))
            .where(new WhereClause()
            .andCompare("", TDbQueues.queueId.getName(), "=")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        public static void exec(long queueId){
            IStmtContainer sc = SrvApiAlgs2.getStmtContainer();
            try {
                exec(sc, queueId);
            } finally {
                sc.close();
            }           
        }

        public static void exec(IStmtContainer sc, long queueId) {
            PrepStmt ps = sc.prepareStatement(TDbQueues.PQDeleteAll.class);
            ps.setLong(1, queueId);
            ps.execute();
        }
    }
    
	public TDbQueues() {
		addField(1, id);
		addField(2, queueId);
		addField(3, data);
		
		addPrimaryKey(4, "pk", new String[]{id.getName()}); //$NON-NLS-1$
		addIndex(5, "queue_idx", new String[]{queueId.getName(), id.getName()}, true, true); //$NON-NLS-1$
	}
}
