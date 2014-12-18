/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.BusinessLogic;
import com.triniforce.db.dml.ResSet;
import com.triniforce.dbo.DBOTableDef;
import com.triniforce.qsync.intf.QSyncTaskResult;
import com.triniforce.qsync.intf.QSyncTaskStatus;
import com.triniforce.utils.IName;

public class TQSyncQueues extends DBOTableDef {
	
	static FieldDef id = FieldDef.createScalarField("id", ColumnType.LONG, true);
	static FieldDef syncerId = FieldDef.createScalarField("id_syncer", ColumnType.LONG, true);
	static FieldDef status = FieldDef.createStringField("status", ColumnType.CHAR, 11, true, null);
	
	public TQSyncQueues() {
		addField(1, id);
		addField(2, syncerId);
		addField(3, status);
		addPrimaryKey(4, "pk", new String[]{id.getName()});
	}

	public static class BL extends BusinessLogic{

		@Override
		public Class getTable() {
			return TQSyncQueues.class;
		}

		public ResSet getQueueInfo(long qid) {
			ResSet rs = select(new IName[]{id, syncerId, status}, new IName[]{id}, new Object[]{qid});
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
			update(new IName[]{status}, new Object[]{result.status.name()}, 
					new IName[]{id, syncerId}, new Object[]{result.qid, result.syncerId});
			
		}
    	
    }
	
}
