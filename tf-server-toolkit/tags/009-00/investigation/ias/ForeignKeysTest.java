/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.sql.Connection;
import java.util.List;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.DBTables;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class ForeignKeysTest extends BasicServerTestCase{

	@Override
	public void test() throws Exception {
		m_server.enterMode(Mode.Running);
		try{

			int prevMaxIdle = getPool().m_ds.getMaxIdle();
			try{			
				
			Connection con = ApiStack.getInterface(Connection.class);
			
			ActualStateBL as = new ActualStateBL(con);
			DBTables tabs = new DBTables();
			tabs.setActualState(as);
			
			TableDef testDef1 = new TableDef("tab1");
			testDef1.addScalarField(1, "f1", ColumnType.INT, true, null);
			testDef1.addPrimaryKey(2, "pk", new String[]{"f1"});
			tabs.add(testDef1);

			List<DBOperation> cl = tabs.getCommandList();
			UpgradeRunner player = new UpgradeRunner(con, as);
			player.run(cl);
			
			con.commit();
			
			
			ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
			trf.push();
			SrvApiAlgs2.getIServerTran().commit();
			trf.pop();
			getPool().m_ds.setMaxIdle(0);
			
			TableDef testDef2 = new TableDef("tab2");
			testDef2.addScalarField(1, "f1", ColumnType.INT, true, null);
			testDef2.addForeignKey(2, "fk", new String[]{"f1"}, "tab1", "pk", false);
			tabs.add(testDef2);
			
			cl = tabs.getCommandList();
			player.run(cl);
			}finally{
				getPool().m_ds.setMaxIdle(prevMaxIdle);				
			}
			
		}finally{
			m_server.leaveMode();
		}
	}
}
