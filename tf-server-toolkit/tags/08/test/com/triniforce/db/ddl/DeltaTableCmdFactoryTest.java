/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.util.Arrays;
import java.util.List;

import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.Delta.AddTabCmd;
import com.triniforce.db.ddl.Delta.ColumnOperationObjectFactory;
import com.triniforce.db.ddl.Delta.DeltaTableCmdFactory;
import com.triniforce.db.ddl.Delta.DropTabCmd;
import com.triniforce.db.ddl.Delta.EditTabCmd;
import com.triniforce.db.ddl.DiffLeader.ICmdFactory;
import com.triniforce.db.ddl.DiffLeader.ICmdFactory.EUnsuccessOperation;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.TFTestCase;

public class DeltaTableCmdFactoryTest extends TFTestCase {

	public void testAddCmd() {
		DeltaTableCmdFactory fact = new Delta.DeltaTableCmdFactory();
		TableDef tab = new TableDef("tab_No1");
		tab.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
		tab.addPrimaryKey(2, "pk", new String[]{"f1"});
		tab.addIndex(3, "idx", new String[]{"f1"}, false, true);
		tab.addForeignKey(4, "fk", new String[]{"f1"}, "parentTab", "parentKey", false);
		AddTabCmd cmd = fact.addCmd(tab);
		assertNotNull(cmd);
		
		List<DBOperation> opList = cmd.toDBOperationList();
		DBOperation op = opList.get(0);
		assertEquals("tab_No1", op.getDBOName());
		CreateTableOperation createOp = (CreateTableOperation) op.getOperation();
		assertNotNull(createOp);
		
		List<TableUpdateOperation> elements = createOp.getElements();
		AddColumnOperation op1 = (AddColumnOperation) elements.get(0);
		assertEquals("f1", op1.getField().getName());
		AddPrimaryKeyOperation op2 = (AddPrimaryKeyOperation) elements.get(1);
		assertEquals("pk", op2.getIndex().getName());
		
		TableOperation op4 = opList.get(1).getOperation();
		assertTrue(Arrays.asList("fk", "idx").contains(op4.getName()));
		op4 = opList.get(2).getOperation();
		assertTrue(Arrays.asList("fk", "idx").contains(op4.getName()));
	}

	public void testDropCmd() {
		DeltaTableCmdFactory fact = new Delta.DeltaTableCmdFactory();
		TableDef tab = new TableDef("tab_No2");
		tab.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
		tab.addPrimaryKey(2, "pk", new String[]{"f1"});
		tab.addIndex(3, "idx", new String[]{"f1"}, false, true);
		tab.addForeignKey(4, "fk", new String[]{"f1"}, "parentTab", "parentKey", false);
		DropTabCmd cmd = fact.dropCmd(tab);
		
		List<DBOperation> opList = cmd.toDBOperationList();
		
		DeleteIndexOperation op1 = (DeleteIndexOperation) opList.get(0).getOperation();
		assertTrue(Arrays.asList("fk", "idx").contains(op1.getName()));
		
		DeleteIndexOperation op2 = (DeleteIndexOperation) opList.get(1).getOperation();
		assertTrue(Arrays.asList("fk", "idx").contains(op2.getName()));
	}
	
	public void testOperationObjectFactory(){
		ColumnOperationObjectFactory f = new Delta.ColumnOperationObjectFactory();
		FieldDef def = FieldDef.createScalarField("f1", ColumnType.INT, true);
		AddColumnOperation res = (AddColumnOperation) f.addCmd(def);
		assertSame(def, res.getField());

		DeleteColumnOperation res2 = (DeleteColumnOperation) f.dropCmd(def);
		assertSame(def, res2.getDeletedField());
		
		assertEquals(ICmdFactory.Action.NONE, f.getEqKeyAction(def, def));
		
		FieldDef def2 = FieldDef.createScalarField("f1", ColumnType.FLOAT, true);
		
		try{
			f.getEqKeyAction(def, def2);
			fail();
		} catch(EUnsuccessOperation e){
			
		}
	}

	public void testEditCmd() {
		DeltaTableCmdFactory fact = new Delta.DeltaTableCmdFactory();
		{
			
			TableDef src = new TableDef("tab_No2");
			src.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			src.addField(2, FieldDef.createScalarField("f2", ColumnType.INT, true));
			src.addPrimaryKey(3, "pk", new String[]{"f1"});
	
			TableDef dst = new TableDef("tab_No2");
			dst.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			dst.addField(2, FieldDef.createScalarField("f3", ColumnType.INT, true));
			dst.addField(3, FieldDef.createScalarField("f4", ColumnType.INT, true));
			dst.addPrimaryKey(4, "pk", new String[]{"f1"});
			dst.addIndex(5, "idx1", new String[]{"f3"}, true, true);
			
			EditTabCmd cmd = fact.editCmd(src, dst);
			List<DBOperation> opList = cmd.toDBOperationList();
			
			AddColumnOperation op1 = (AddColumnOperation) opList.get(0).getOperation();
			assertEquals("f3", op1.getField().getName());
			op1 = (AddColumnOperation) opList.get(1).getOperation();
			assertEquals("f4", op1.getField().getName());
			DeleteColumnOperation op2 = (DeleteColumnOperation) opList.get(2).getOperation();
			assertEquals("f2", op2.getDeletedField().getName());
			
			AddIndexOperation op3 = (AddIndexOperation) opList.get(3).getOperation();
			assertEquals("idx1", op3.getIndex().getName());
		}		
		
		{// table have same indexes, but other names
			TableDef src = new TableDef("tab_No2");
			src.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			src.addPrimaryKey(2, "pk", new String[]{"f1"});
			TableDef dst = new TableDef("tab_No2");
			dst.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			dst.addPrimaryKey(2, "_pk_01", new String[]{"f1"});
			assertTrue(fact.editCmd(src, dst).toDBOperationList().isEmpty());;
		}
		{// indexes have same fields but other types
			TableDef src = new TableDef("tab_No2");
			src.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			src.addPrimaryKey(2, "pk", new String[]{"f1"});
			TableDef dst = new TableDef("tab_No2");
			dst.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			dst.addForeignKey(2, "fk", new String[]{"f1"}, "tab_no_2", "pk", false);
			
			List<DBOperation> commands = fact.editCmd(src, dst).toDBOperationList();
			assertEquals(2, commands.size());
		}
		{// indexes have same fields but other parents
			TableDef src = new TableDef("tab_No2");
			src.setSupportForeignKeys(true);
			src.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			src.addForeignKey(2, "fk", new String[]{"f1"}, "tab_no_1", "pk", false);
			TableDef dst = new TableDef("tab_No2");
			dst.setSupportForeignKeys(true);
			dst.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			dst.addForeignKey(2, "fk", new String[]{"f1"}, "tab_no_2", "pk", false);
			
			List<DBOperation> commands = fact.editCmd(src, dst).toDBOperationList();
			assertEquals(2, commands.size());
		}
		{	// source table doesn't support FK
			TableDef src = new TableDef("tab_No2");
			src.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			src.addIndex(2, "fk", new String[]{"f1"}, true, true);
			src.setSupportForeignKeys(false);
			TableDef dst = new TableDef("tab_No2");
			dst.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			dst.addForeignKey(2, "fk", new String[]{"f1"}, "tab_no_3", "pk", false);
			List<DBOperation> commands = fact.editCmd(src, dst).toDBOperationList();
			assertEquals(0, commands.size());
		}
		
	}

	public void testGetEqKeyAction() {
		DeltaTableCmdFactory fact = new Delta.DeltaTableCmdFactory();
		assertEquals(ICmdFactory.Action.EDIT, fact.getEqKeyAction(new TableDef("tab_No1"), new TableDef("tab_No1")));
	}

}
