/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.db.test;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;

import com.triniforce.db.ddl.AddColumnOperation;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

/**
 * Issues with statements
 * 
 */
public class StatementsTest extends DBTestCase {

    String tabName;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tabName = this.createTableIfNeeded(new TableDef(
        "TransactionTest.testStatements").addModification(1,
        new AddColumnOperation(FieldDef.createScalarField("i",
                ColumnType.INT, false))));        
    }
    
    public void testStatements() throws EDBObjectException, Exception {

        // Checks that close() can be called for any statement
        {
            PreparedStatement stmtPrepared = getConnection().prepareStatement(
                    "insert into " + tabName + " values(1)");
            stmtPrepared.execute();
            Statement stmt = getConnection().createStatement();
            stmt.execute("select * from " + tabName);

            stmt.close();
            stmtPrepared.close();
            getConnection().commit();
        }
    }
    
    public void testSetNull() throws Exception{
        PreparedStatement stmtDelete = getConnection().prepareStatement(
                "delete from " + tabName);        
        stmtDelete.execute();
        PreparedStatement stmtSelect = getConnection().prepareStatement("select * from " + tabName);
        assertFalse(stmtSelect.executeQuery().next());
        
        PreparedStatement stmtPrepared = getConnection().prepareStatement(
                "insert into " + tabName + " values(?)");
        stmtPrepared.setNull(1, Types.VARCHAR);        
        //stmtPrepared.setNull(1, Types.INTEGER);
        stmtPrepared.execute();
        assertTrue(stmtSelect.executeQuery().next());        
       
        
    }
}
