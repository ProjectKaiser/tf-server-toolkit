/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.util.Arrays;

import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QTable;
import com.triniforce.db.test.TFTestCase;

public class ViewDefTest extends TFTestCase {

	@Override
	public void test() throws Exception {
		QSelect qsel = new QSelect().joinLast(new QTable("table_nnfsd"));
		ViewDef def = new ViewDef("v1", Arrays.asList("c1", "c2", "c3"), qsel);
		assertEquals("create view v1 (c1, c2, c3) as select from table_nnfsd", def.toString());
	}
}
