/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.BusinessLogic;
import com.triniforce.db.dml.IStmtContainer;
import com.triniforce.db.dml.PrepSql;
import com.triniforce.db.dml.PrepStmt;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.qbuilder.Expr;
import com.triniforce.db.qbuilder.Expr.EqKind;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QStatement;
import com.triniforce.db.qbuilder.WhereClause;
import com.triniforce.dbo.DBOTableDef;
import com.triniforce.qsync.intf.QSyncTaskResult;
import com.triniforce.qsync.intf.QSyncTaskStatus;
import com.triniforce.server.plugins.kernel.SrvTable;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IName;

public class TQSyncQueues extends DBOTableDef {
	
	static final int MAX_ERROR_MESSAGE = 250;
	static final int MAX_ERROR_STACK = 4048;
	
	static FieldDef id = FieldDef.createScalarField("id", ColumnType.LONG, true);
	static FieldDef syncerId = FieldDef.createScalarField("id_syncer", ColumnType.LONG, true);
	static FieldDef status = FieldDef.createStringField("status", ColumnType.CHAR, 20, true, null);
	static FieldDef errorClass = FieldDef.createStringField("error_class", ColumnType.VARCHAR, 255, false, null);
	static FieldDef errorMessage = FieldDef.createStringField("error_message", ColumnType.NVARCHAR, 255, false, null);
	static FieldDef errorStackTrace = FieldDef.createStringField("error_stackTrace", ColumnType.VARCHAR, 4048, false, null);
	
	public TQSyncQueues() {
		addField(1, id);
		addField(2, syncerId);
		addField(3, status);
		addPrimaryKey(4, "pk", new String[]{id.getName()});
		addField(5, errorClass);
		addField(6, errorMessage);
		addField(7, errorStackTrace);
	}

	public static class BL extends BusinessLogic{

		@Override
		public Class getTable() {
			return TQSyncQueues.class;
		}

		public ResSet getQueueInfo(long qid) {
			ResSet rs = select(new IName[]{id, syncerId, status, errorClass, errorMessage, errorStackTrace}, new IName[]{id}, new Object[]{qid});
			return rs;
		}

		public void registerQueue(long qid, long sncId, QSyncTaskStatus vstatus) {
			insert(new IName[]{id, syncerId, status}, new Object[]{qid, sncId, vstatus.name()});
			
		}

		public void dropQueue(long qid) {
			delete(new IName[]{id}, new Object[]{qid});
			
		}

		public void updateQueueStatus(long qid, QSyncTaskStatus vstatus) {
			update(new IName[]{status}, new Object[]{vstatus.name()},
					new IName[]{id}, new Object[]{qid});
			
		}

		public ResSet getQueues() {
			ResSet rs = select(new IName[]{id, syncerId, status}, new IName[]{}, new Object[]{});
			return rs;
		}

		public void taskCompleted(QSyncTaskResult result) {
			ApiAlgs.getLog(this).trace(result.toString());
			String errMsg = cutStringIfExceeded(result.errorMessage, MAX_ERROR_MESSAGE);
			String errStack = cutStringIfExceeded(result.errorStack, MAX_ERROR_STACK);
			update(new IName[]{status,errorClass, errorMessage, errorStackTrace}, 
					new Object[]{result.status.name(),  result.errorClass,errMsg,errStack},
					//result.errorClass, result.errorMessage,result.errorStack}, 
					new IName[]{id, syncerId}, new Object[]{result.qid, result.syncerId});
			
		}

		private String cutStringIfExceeded(String str,
				int max) {
			if(str.length() > max){
				str= str.substring(0, max);
			}
			return str;
		}

		public ResSet getIds() {
			ResSet rs = select(new IName[]{id}, new IName[]{}, new Object[]{});
			return rs;
		}

		public void clear() {
			delete(new IName[]{}, new Object[]{});
		}
		
		
		public static class PQSelectExclude extends PrepSql{
			@Override
			public QStatement buildSql() {
				return new QSelect().joinLast(new SrvTable(TQSyncQueues.class).addCol(id).addCol(syncerId).addCol(status))
						.where(new WhereClause().and(new Expr.Compare(new Expr.Column("", id.getName()), EqKind.NE, new Expr.Param())));
			}
			
			static ResSet exec(IStmtContainer sc, long qid){
	            PrepStmt ps = sc.prepareStatement(PQSelectExclude.class);
	            ps.setLong(1, qid);
	            return ps.executeQuery();
				
			}
		}

		public ResSet getQueuesExclude(long excludedQueue) {
			return PQSelectExclude.exec(getSt(), excludedQueue);
		}
		
		
    	
    }
	
}
