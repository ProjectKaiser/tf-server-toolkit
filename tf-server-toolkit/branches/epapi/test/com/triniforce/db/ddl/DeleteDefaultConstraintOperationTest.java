/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import junit.framework.TestCase;

public class DeleteDefaultConstraintOperationTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDeleteDefaultConstraintOperation() {
        DeleteDefaultConstraintOperation op = new DeleteDefaultConstraintOperation("name", "column", "template");
        assertEquals("name", op.getName());
        assertEquals("column", op.getColumnName());
        assertEquals(0, op.getVersionIncrease());
        assertTrue(op.getReverseOperation() instanceof AddDefaultConstraintOperation);
    }

}
