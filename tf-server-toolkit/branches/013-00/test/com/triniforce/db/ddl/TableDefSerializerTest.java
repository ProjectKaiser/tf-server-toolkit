/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.triniforce.db.ddl.TableDef.ElementVerStored;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.TFTestCase;

public class TableDefSerializerTest extends TFTestCase {

	@Override
	public void test() throws Exception {
		TableDefSerializer srz = new TableDefSerializer();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(out);
		TableDef td = new TableDef("table_1");
		td.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
		td.addPrimaryKey(2, "pk", new String[]{"f1"});
		srz.writeDef(td, oout);
		oout.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		ObjectInputStream oin = new ObjectInputStream(in);
		TableDef res = srz.readDef(oin);
		assertNotNull(res);
		assertEquals("table_1", res.getEntityName());
		assertNotNull(res.getFields().findElement("f1"));
		ElementVerStored<IndexDef> idx = res.getIndices().findElement("pk");
		assertNotNull(idx);
		assertEquals(IndexDef.TYPE.PRIMARY_KEY, idx.getElement().getType());
		
		assertNull(srz.readDef(oin));
	}
}
