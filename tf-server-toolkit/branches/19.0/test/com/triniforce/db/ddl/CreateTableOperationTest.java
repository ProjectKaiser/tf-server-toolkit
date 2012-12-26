/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.util.ArrayList;
import java.util.Arrays;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

import junit.framework.TestCase;

public class CreateTableOperationTest extends TestCase {
	
	CreateTableOperation m_op;
	
	@Override
	protected void setUp() throws Exception {
		ArrayList<TableUpdateOperation> elements = new ArrayList<TableUpdateOperation>();
		elements.add(new AddColumnOperation(FieldDef.createScalarField("f1", ColumnType.FLOAT, true)));
		elements.add(new AddPrimaryKeyOperation("pk1", Arrays.asList("f1")));
		elements.add(new AddForeignKeyOperation("fk1", Arrays.asList("f1"), "parentTab", "pk1"));
		m_op = new CreateTableOperation(elements);
		super.setUp();
	}

	public void testCreateTableOparation(){
		assertEquals("CREATE_TABLE", m_op.getName());
		assertEquals(3, m_op.getVersionIncrease());
		
		TableOperation delTabOp = m_op.getReverseOperation();
		assertNotNull(delTabOp);
		assertTrue(delTabOp instanceof DropTableOperation);
		assertEquals("DROP_TABLE", delTabOp.getName());
	}
	
	public void testApply() throws EDBObjectException{
		try{
			TableDef tab = new TableDef("table");
			m_op.apply(tab);
			fail();
		}catch(EMetadataException e){}			
	}
}
