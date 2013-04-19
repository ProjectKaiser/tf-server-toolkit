/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.db.test;

import java.sql.ResultSet;

import com.triniforce.db.ddl.AddColumnOperation;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

/**
 * Test misc things linked to transactions
 * 
 */
public class TransactionTest extends DBTestCase {

    /**
     * Test that table support transaction. Needed since not all MySQL table
     * types support transactions
     * 
     * @throws Exception
     */
    public void testTranTable() throws Exception {
        String tabName = this.createTableIfNeeded(new TableDef(
                "TransactionTest.testTranTable").addModification(1,
                new AddColumnOperation(FieldDef.createScalarField("i",
                        ColumnType.INT, false))));

        getConnection().setAutoCommit(false);
        getConnection().commit();

        // check there are no rows in table
        {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "select i from " + tabName);
            if (rs.next())
                fail();
        }

        // insert one row
        getConnection().prepareStatement(
                "insert into " + tabName + " values (1)").executeUpdate();
        getConnection().createStatement().executeQuery(
                "select i from " + tabName);

        // make sure there is one row
        {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "select i from " + tabName);
            if (!rs.next())
                fail();
            if (rs.next())
                fail();
        }

        getConnection().rollback();

        // check there are no rows in table
        {
            ResultSet rs = getConnection().createStatement().executeQuery(
                    "select i from " + tabName);
            if (rs.next())
                fail();
        }
    }

}
