/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.export;

import java.util.HashMap;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.datatype.DataType;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.ActualStateBL.TIndexNames;
import com.triniforce.db.test.TFTestCase;

public class MappedDataSetTest extends TFTestCase {

    @Override
	public void test() throws DataSetException {
		HashMap<String, String> realAppDbNameMap = new HashMap<String, String>();
		realAppDbNameMap.put("APP_T1", "db2_t1");
		DefaultDataSet src = new DefaultDataSet();
		{
    		//act_state
    		DefaultTable act_state = new DefaultTable(ActualStateBL.ACT_STATE_TABLE, new Column[]{
    				new Column(ActualStateBL.APPNAME, DataType.VARCHAR),
    				new Column(ActualStateBL.DBNAME, DataType.VARCHAR)
    		});
    		act_state.addRow(new Object[]{ActualStateBL.ACT_STATE_TABLE, ActualStateBL.ACT_STATE_TABLE});
    		act_state.addRow(new Object[]{"APP_T1", "db1_t1"});
    		act_state.addRow(new Object[]{TIndexNames.class.getName(), "db1_t2"});
    		src.addTable(act_state);
    		
    		//t1
    		DefaultTable t1 = new DefaultTable("db1_t1", new Column[]{
    				new Column("id", DataType.INTEGER)
    		});
    		t1.addRow(new Object[]{1101});
    		t1.addRow(new Object[]{1102});
    		src.addTable(t1);
    
    		//t2		
    		DefaultTable t2 = new DefaultTable("db1_t2", new Column[]{
    				new Column("id", DataType.INTEGER)
    		});
    		src.addTable(t2);
		}
		
		MappedDataSet res = new MappedDataSet(realAppDbNameMap, src);
		try{
			res.getTable("T_UNK");
			fail();
		} catch(NoSuchTableException e){}
		
		ITable t1_dst = res.getTable("db2_t1");
		assertEquals(2, t1_dst.getRowCount());
		assertEquals("db2_t1", t1_dst.getTableMetaData().getTableName());
		assertEquals(1101, t1_dst.getValue(0, "id"));
		assertEquals(1102, t1_dst.getValue(1, "id"));
		
		assertFalse(res.getAppTables().contains(ActualStateBL.ACT_STATE_TABLE));
		assertFalse(res.getAppTables().contains(TIndexNames.class.getName()));
	}

}
