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
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.TFTestCase;

public class DeltaTest extends TFTestCase {

	public void testCalculateDelta() {
		Delta delta = new Delta();
		{
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
		
		{// Test original index names
			//Src already have index on COL1 but dst add new Original index on same column
			HashMap<String, TableDef> src = new HashMap<String, TableDef>();
			HashMap<String, TableDef> dst = new HashMap<String, TableDef>();
			TableDef t1 = new TableDef("t1");
			t1.addField(1, FieldDef.createScalarField("col1", ColumnType.INT,true));
			t1.addModification(2, new AddIndexOperation(IndexDef.createIndex("IDX1", Arrays.asList("col1"), false, true, false)));
			src.put("t1", t1);
			TableDef t1_2 = new TableDef("t1");
			t1_2.addField(1, FieldDef.createScalarField("col1", ColumnType.INT,true));
			t1_2.addModification(2, new AddIndexOperation(IndexDef.createIndex("IDX1", Arrays.asList("col1"), false, true, false)));
			t1_2.addModification(3, new AddIndexOperation(IndexDef.createIndex("IDX2_ORIGINAL", Arrays.asList("col1"), false, true, false, null, null, true)));
			dst.put("t1", t1_2);
			
			List<DBOperation> res = delta.calculateDelta(src, dst);
			AddIndexOperation op1 = (AddIndexOperation) res.get(0).getOperation();
			assertNotNull(op1);
			assertEquals("IDX2_ORIGINAL", op1.getIndex().getName());
		}
		
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
	
	
	public void testEditTable(){
		Delta delta = new Delta();
		DeltaSchema src = new Delta.DeltaSchema();
		DeltaSchema dst = new Delta.DeltaSchema();
		
		TableDef table = new TableDef("testEditTable");
		table.addStringField(1, "fstr", ColumnType.NVARCHAR, 60, false, null);
		src.addTable(table);
		
		table = new TableDef("testEditTable");
		table.addStringField(1, "fstr", ColumnType.VARCHAR, 60, false, null);
		dst.addTable(table);
		
		List<DBOperation> res = delta.calculateDelta(src.getTables(), dst.getTables());
		assertEquals(1, res.size());
		AlterColumnOperation op  = (AlterColumnOperation) res.get(0).getOperation();
		assertNotNull(op);
		assertEquals(ColumnType.VARCHAR, op.getNewField().getType());
	}
	

}
