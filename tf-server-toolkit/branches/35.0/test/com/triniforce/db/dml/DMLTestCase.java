/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */


package com.triniforce.db.dml;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;
import com.triniforce.db.ddl.AddColumnOperation;
import com.triniforce.db.ddl.AddPrimaryKeyOperation;
import com.triniforce.db.ddl.DDLTestCase;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;


public class DMLTestCase extends DDLTestCase {

    public static final String TAB = "Test.DML.TableTest";
    private static String m_dbName;
    private static TableDef m_tabDef = null;    
    private static CachedRowSet m_rs;

    protected void setUp() throws Exception {
        if(this.getClass().equals(DMLTestCase.class)) return;
        super.setUp();
        
        m_tabDef = new TableDef(TAB);
        m_tabDef.addModification(1, new AddColumnOperation(FieldDef.createScalarField("id", ColumnType.INT, true, "0")));
        m_tabDef.addModification(2, new AddColumnOperation(FieldDef.createStringField("name", ColumnType.VARCHAR, 20, false, null)));
        m_tabDef.addModification(3, new AddColumnOperation(FieldDef.createStringField("phone", ColumnType.VARCHAR, 20, false, "123-45-67")));
        m_tabDef.addModification(4, new AddColumnOperation(FieldDef.createStringField("description", ColumnType.VARCHAR, 20, false, "--DD--")));
        m_tabDef.addModification(5, new AddPrimaryKeyOperation("pk1", Arrays.asList("id")));
                    
        m_dbName = createTableIfNeeded(m_tabDef);

        PreparedStatement ps = getConnection().prepareStatement("insert into "+m_dbName+"(ID, Name, Phone, Description) values (?,?,?,?)");            
        ps.setInt(1, 0);
        ps.setString(2, "Alex White");
        ps.setString(3, "555-92-92");
        ps.setString(4, "no comment");
        ps.addBatch();
        ps.setInt(1, 1);
        ps.setString(2, "Serg Bake");
        ps.setString(3, "531-92-92");
        ps.setString(4, "--DD--");
        ps.addBatch();
        ps.setInt(1, 2);
        ps.setString(2,  "James Bond");
        ps.setString(3, "007");
        ps.setString(4, "hi");
        ps.addBatch();
        ps.executeBatch();
        
        m_rs = getCurrentRS();
        assertEquals(3, m_rs.size());
    }   
    
    TableDef getTabDef(){
        return m_tabDef;
    }
    
    String getDbName(){
        return m_dbName;
    }
    
    CachedRowSet getRS(){
        return m_rs;
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(this.getClass().equals(DMLTestCase.class)) return;
        super.tearDown();
    }
    
    CachedRowSet getCurrentRS() throws Exception{
        Statement stmnt = getConnection().createStatement();        
        ResultSet rs = stmnt.executeQuery("SELECT * FROM "+getDbName()+" ORDER BY ID");
        CachedRowSet crs = new CachedRowSetImpl();
        crs.populate(rs);
        rs.close();
        stmnt.close();        
        return crs;        
    }

}
