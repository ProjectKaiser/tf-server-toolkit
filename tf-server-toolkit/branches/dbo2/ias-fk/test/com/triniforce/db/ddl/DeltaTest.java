/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.Delta.DeltaSchema;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.TFTestCase;

public class DeltaTest extends TFTestCase {

	public void testCalculateDelta() {
		Delta delta = new Delta();
		DeltaSchema src = new Delta.DeltaSchema();
		DeltaSchema dst = new Delta.DeltaSchema();
		
		List<DBOperation> res = delta.calculateDelta(src.getTables(), dst.getTables());
		assertTrue(res.isEmpty());
		
		TableDef table = new TableDef("t1");
		FieldDef fld = FieldDef.createScalarField("fld1", ColumnType.INT, true);
		table.addField(1, fld);
		table.addField(2, FieldDef.createScalarField("fld2", ColumnType.INT, true));
		dst.addTable(table);
		
		{
			table = new TableDef("t2");
			FieldDef fld21 = FieldDef.createScalarField("fld21", ColumnType.INT, true);
			table.addField(1, fld21);
			src.addTable(table);
			
			table = new TableDef("t2");
			table.addField(1, fld21);
			FieldDef fld22 = FieldDef.createScalarField("fld22", ColumnType.INT, true);
			table.addField(2, fld22);
			table.addForeignKey(3, "fk", new String[]{"fld22"}, "t1", "pk", false);
			dst.addTable(table);
		}
		{
			table = new TableDef("t3");
			FieldDef fld21 = FieldDef.createScalarField("fld31", ColumnType.INT, true);
			table.addField(1, fld21);
			src.addTable(table);
		}
		
		res = delta.calculateDelta(src.getTables(), dst.getTables());
		
		assertEquals(4, res.size());
		
		List<TableOperation> ops1 = getTabOps(res, "t1");
		
		CreateTableOperation op = (CreateTableOperation) ops1.get(0);
		AddColumnOperation op2 = (AddColumnOperation) op.getElements().get(0);
		assertSame(fld, op2.getField());
		
		DBOperation lastOp = res.get(res.size()-1);
		assertEquals("t2", lastOp.getDBOName());
		
	}

	private List<TableOperation> getTabOps(List<DBOperation> ops, String dbo) {
		ArrayList<TableOperation> res = new ArrayList<TableOperation>();
		for (DBOperation operation : ops) {
			if(operation.getDBOName().equals(dbo))
				res.add(operation.getOperation());
		}
		return res;
	}
	
	public void testCalculateTabDefs(){
		HashMap<String, TableDef> sch = new HashMap<String, TableDef>();
		TableDef table = new TableDef("pkg1.testCalculateTabDefs");
		table.addScalarField(1, "field1", ColumnType.INT, false, null);
		sch.put(table.getEntityName(), table);
		table = new TableDef("pkg1.testCalculateTabDefs2");
		table.addScalarField(1, "field1", ColumnType.INT, false, null);
		sch.put(table.getEntityName(), table);
		ArrayList<DBOperation> commands = new ArrayList<DBOperation>();
		commands.add(new DBOperation("pkg1.testCalculateTabDefs", 
				new AddColumnOperation(FieldDef.createScalarField("field2", ColumnType.INT, false))));
		commands.add(new DBOperation("pkg1.testCalculateTabDefs2", 
				new AddColumnOperation(FieldDef.createScalarField("field2", ColumnType.INT, false))));
		commands.add(new DBOperation("pkg1.testCalculateTabDefs3",
				new CreateTableOperation(Arrays.asList( (TableUpdateOperation)
						new AddColumnOperation(FieldDef.createScalarField("field_2", ColumnType.DOUBLE, false))
				))
		));
		Delta delta = new Delta();
		
		delta.applyCommands(sch, commands);
		assertEquals(3, sch.size());
		table = sch.get("pkg1.testCalculateTabDefs");
		assertEquals(2, table.getVersion());
		table = sch.get("pkg1.testCalculateTabDefs2");
		assertEquals(2, table.getVersion());
	}

}
