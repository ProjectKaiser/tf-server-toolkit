/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */


package com.triniforce.db.dml;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.rowset.CachedRowSet;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.AddColumnOperation;
import com.triniforce.db.ddl.AddIndexOperation;
import com.triniforce.db.ddl.AddPrimaryKeyOperation;
import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.DBTables;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.Table.Row;
import com.triniforce.db.dml.TableAdapter.WhereClause;

public class TableAdapterTest extends DMLTestCase {

    private TableAdapter m_adapter;

    protected void setUp() throws Exception {
        super.setUp();
        m_adapter = new TableAdapter();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testLoad() throws SQLException{
        Table tab = new Table();
        m_adapter.load(tab, getRS());
    }
    
    public void testGetDeleteStatementString(){}
    
    public void testWhereCondition() throws EDBObjectException{
        {//by primary key
            Table tab = new Table();
            tab.addColumn(getTabDef().getFields().getElement(0));
            tab.addColumn(getTabDef().getFields().getElement(1));
            tab.addColumn(getTabDef().getFields().getElement(2));
            WhereClause where = new WhereClause(getTabDef(), tab.getFieldDefs());
            assertEquals("id=?", where.m_whereStr);
            assertEquals(1, where.m_fNums.length);
            assertEquals(0, where.m_fNums[0]);
        }
        {//by columns
            Table tab = new Table();
            tab.addColumn(getTabDef().getFields().getElement(1));
            tab.addColumn(getTabDef().getFields().getElement(2));
            WhereClause where = new WhereClause(getTabDef(), tab.getFieldDefs());
            assertEquals("name=? AND phone=?", where.m_whereStr);
            assertEquals(2, where.m_fNums.length);
            assertEquals(0, where.m_fNums[0]);
            assertEquals(1, where.m_fNums[1]);
        }
        {
            TableDef cplxDef = new TableDef("complexDEF");
            cplxDef.addModification(1, new AddColumnOperation(FieldDef.createScalarField("colPK1", ColumnType.INT, true, 0)));
            cplxDef.addModification(2, new AddColumnOperation(FieldDef.createScalarField("colPK2", ColumnType.INT, true, 0)));
            cplxDef.addModification(3, new AddColumnOperation(FieldDef.createScalarField("colUI",  ColumnType.INT, true, 0)));
            cplxDef.addModification(4, new AddColumnOperation(FieldDef.createScalarField("colSMP1", ColumnType.INT, true, 0)));
            cplxDef.addModification(5, new AddColumnOperation(FieldDef.createScalarField("colSMP2", ColumnType.INT, true, 0)));
            cplxDef.addModification(6, new AddColumnOperation(FieldDef.createScalarField("colSMP3", ColumnType.INT, true, 0)));
            cplxDef.addModification(7, new AddIndexOperation(IndexDef.createIndex("ui1", Arrays.asList("colUI"), true, true)));
            cplxDef.addModification(8, new AddPrimaryKeyOperation("pk", Arrays.asList("colPK1", "colPK2")));
            {   //search by PK
                Table t1 = new Table();
                t1.addColumn(cplxDef.getFields().getElement(4));
                t1.addColumn(cplxDef.getFields().getElement(0));
                t1.addColumn(cplxDef.getFields().getElement(3));
                t1.addColumn(cplxDef.getFields().getElement(1));
                t1.addColumn(cplxDef.getFields().getElement(2));
                WhereClause where = new WhereClause(cplxDef, t1.getFieldDefs());
                assertEquals("colPK1=? AND colPK2=?", where.m_whereStr);
                assertEquals(2, where.m_fNums.length);
                assertEquals(1, where.m_fNums[0]);
                assertEquals(3, where.m_fNums[1]);                
            }
            {   //search by Unique index (no primary keys)
                Table t1 = new Table();
                t1.addColumn(cplxDef.getFields().getElement(4));
                t1.addColumn(cplxDef.getFields().getElement(3));
                t1.addColumn(cplxDef.getFields().getElement(1));
                t1.addColumn(cplxDef.getFields().getElement(2));
                WhereClause where = new WhereClause(cplxDef, t1.getFieldDefs());
                assertEquals("colUI=?", where.m_whereStr);
                assertEquals(1, where.m_fNums.length);
                assertEquals(3, where.m_fNums[0]);
            }
            {   //search by values (no unique indices)
                Table t1 = new Table();
                t1.addColumn(cplxDef.getFields().getElement(4));
                t1.addColumn(cplxDef.getFields().getElement(3));
                t1.addColumn(cplxDef.getFields().getElement(1));
                t1.addColumn(cplxDef.getFields().getElement(5));
                WhereClause where = new WhereClause(cplxDef, t1.getFieldDefs());
                assertEquals("colSMP2=? AND colSMP1=? AND colPK2=? AND colSMP3=?", where.m_whereStr);
                assertEquals(4, where.m_fNums.length);
                assertEquals(0, where.m_fNums[0]);
                assertEquals(1, where.m_fNums[1]);
                assertEquals(2, where.m_fNums[2]);
                assertEquals(3, where.m_fNums[3]);
            }
        }
    }
    
    private Table getTestTable() throws SQLException{
        Table tab = new Table();
        tab.addColumn(getTabDef().getFields().getElement(0));
        tab.addColumn(getTabDef().getFields().getElement(1));
        tab.addColumn(getTabDef().getFields().getElement(2));
        tab.populate(getRS());
        return tab;
    }
    
    public void testGetDeletePattern() throws SQLException{
        Table tab = getTestTable();        
        WhereClause where = new WhereClause(getTabDef(), tab.getFieldDefs());
        String pattern = m_adapter.getDeletePattern(tab, getDbName(), where.m_whereStr);
        assertEquals("DELETE FROM "+getDbName()+" WHERE id=?", pattern);;
    }
    
    public void testGetInsertPattern() throws SQLException{
        Table tab = getTestTable();        
        String pattern = m_adapter.getInsertPattern(tab, getDbName());
        assertEquals("INSERT INTO "+getDbName()+" (ID,NAME,PHONE) VALUES (?,?,?)", pattern);
    }
    
    public void testGetUpdatePattern() throws SQLException{
        Table tab = getTestTable();        
        WhereClause where = new WhereClause(getTabDef(), tab.getFieldDefs());
        String pattern = m_adapter.getUpdatePattern(tab, getDbName(), where.m_whereStr);
        assertEquals("UPDATE "+getDbName()+" SET ID=?,NAME=?,PHONE=? WHERE id=?", pattern);
    }
    
    public void testFlush1Type() throws Exception{
        Table tab = getTestTable();
        {   //test delete
            tab.getRow(0).delete();
            tab.getRow(2).delete();
            m_adapter.flush(getConnection(), tab, getTabDef(), getDbName());
            CachedRowSet rs = getCurrentRS();
            assertEquals(1, rs.size());
            rs.absolute(1);
            assertEquals(1, rs.getInt("ID"));
        }
        {   //test insert
            Row r = tab.newRow();
            r.setField("ID", 12);
            r.setField("Name", "Topor Nuraev");
            r.setField("Phone", "761239-7390");
            r = tab.newRow();
            r.setField("ID", 13);
            r.setField("Name", "Nuraz Sukuranchy");
            m_adapter.flush(getConnection(), tab, getTabDef(), getDbName());
            CachedRowSet rs = getCurrentRS();
            assertEquals(3, rs.size());
            rs.absolute(2);
            assertEquals(12, rs.getInt("ID"));
            assertEquals("Topor Nuraev", rs.getString("Name"));
            assertEquals("761239-7390", rs.getString("Phone"));
            rs.absolute(3);
            assertEquals(13, rs.getInt("ID"));
            assertEquals("Nuraz Sukuranchy", rs.getString("Name"));
            assertEquals("123-45-67", rs.getString("Phone"));
        }
        {   //test update
            Row r = tab.getRow(0);
            r.setField("ID", 5);
            r.setField("Name", "Sikura Niemi");
            r.setField("Phone", "622-78-98");
            m_adapter.flush(getConnection(), tab, getTabDef(), getDbName());
            CachedRowSet rs = getCurrentRS();
            assertEquals(3, rs.size());
            rs.absolute(1);
            assertEquals(5, rs.getInt("ID"));
            assertEquals("Sikura Niemi", rs.getString("Name"));
            assertEquals("622-78-98", rs.getString("Phone"));
        }
    }
    

    public void testFlush() throws Exception{
        
        Table tab = getTestTable();
        
        Row r = tab.newRow();   //insert
        r.setField("ID", 7);
        r.setField("Name", "Hermit Vilhelm");
        r.setField("Phone", "432-89-09");
        r = tab.newRow();       //insert
        r.setField("ID", 10);
        r.setField("Name", "Simone Straus");
        r.setField("Phone", "823-32-09");        
        r = tab.getRow(2);      //update
        r.setField("ID", 11);
        r.setField("Name", "Devone Dimm");
        r = tab.getRow(3);      //delete inserted (cancel)
        r.delete();
        r = tab.getRow(1);      //delete intact
        r.delete();
        
        m_adapter.flush(getConnection(), tab, getTabDef(), getDbName());
        
        ResultSet currentRS = getCurrentRS();
        ResultSet prevRS = getRS();
        
        currentRS.absolute(1);
        prevRS.absolute(1);
        //first row untuchable
        assertEquals(prevRS.getInt("ID"), currentRS.getInt("ID"));
        assertEquals(prevRS.getString("Name"), currentRS.getString("Name"));
        assertEquals(prevRS.getString("Phone"), currentRS.getString("Phone"));
        assertEquals(prevRS.getString("Description"), currentRS.getString("Description"));
        
        //no second row here is inserted
        currentRS.absolute(2);
        assertEquals(10, currentRS.getInt("ID"));
        assertEquals("Simone Straus", currentRS.getString("Name"));
        assertEquals("823-32-09", currentRS.getString("Phone"));
        assertEquals(getTabDef().getFields().getElement(3).getDefaultValue(), currentRS.getString("Description"));
        
        //next updated row 
        currentRS.absolute(3);
        prevRS.absolute(3);
        assertEquals(11, currentRS.getInt("ID"));
        assertEquals("Devone Dimm", currentRS.getString("Name"));
        assertEquals(prevRS.getString("Phone"), currentRS.getString("Phone"));
        assertEquals(prevRS.getString("Description"), currentRS.getString("Description"));
        
        //ok
    }
    
    public void testSetWithNull() throws Exception{
        Table t = getTestTable();
        Row r = t.newRow();
        r.setField(0, 992);
        r.setField(1, "Nuller Vist");
        r.setField(2, null);
        m_adapter.flush(getConnection(), t, getTabDef(), getDbName());
    }

    public void testBlobField() throws Exception{
        TableDef tabDef = new TableDef("Test.DML.TabWithBlob");
        tabDef.addModification(1, new AddColumnOperation(FieldDef.createStringField("name", ColumnType.CHAR, 16, true, "----")));
        tabDef.addModification(2, new AddColumnOperation(FieldDef.createScalarField("content", ColumnType.BLOB, false, null)));
        tabDef.addModification(3, new AddPrimaryKeyOperation("pk1", Arrays.asList("name")));
        DBTables tabs = new DBTables();
        UpgradeRunner pl = new UpgradeRunner(getConnection(), new ActualStateBL(getConnection()));
        tabs.add(tabDef);
        tabs.setActualState(pl.getActualState());
        pl.run(tabs.getCommandList());
        
        getConnection().commit();
        
        Table t = new Table();
        t.addColumn(tabDef.getFields().getElement(0));
        t.addColumn(tabDef.getFields().getElement(1));
        
        Row r = t.newRow();
        r.setField(0, "r1");
        r.setField(1, null);//new SerialBlob(new byte[1]));
        r = t.newRow();
        r.setField(0, "r2");
        r.setField(1, "content1".getBytes());
        TableAdapter a = new TableAdapter();
        
        a.flush(getConnection(), t, tabDef, pl.getActualState().getDBName(tabDef.getEntityName()));
    
        t = new Table();
        t.addColumn(tabDef.getFields().getElement(0));
        t.addColumn(tabDef.getFields().getElement(1));
        a.load(t, getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("select * from "+ pl.getActualState().getDBName(tabDef.getEntityName())));
        assertEquals(2 , t.getSize());
        assertNull(t.getRow(0).getField(1));
        Blob blob = (Blob)t.getRow(1).getField(1);
        assertEquals("content1", new String(blob.getBytes(1, "content1".length())));
    }
    
    public void testInsert() throws Exception{
        Table tab = getTestTable();
        {   //test insert
            Row r = tab.newRow();
            r.setField("ID", 12);
            r.setField("Name", "Topor Nuraev");
            r.setField("Phone", "761239-7390");
            r = tab.newRow();
            r.setField("ID", 13);
            r.setField("Name", "Nuraz Sukuranchy");
            
            m_adapter.insert(getConnection(), tab, getTabDef(), getDbName());
            {//check database update
                CachedRowSet rs = getCurrentRS();
                assertEquals(5, rs.size());
                rs.absolute(4);
                assertEquals(12, rs.getInt("ID"));
                assertEquals("Topor Nuraev", rs.getString("Name"));
                assertEquals("761239-7390", rs.getString("Phone"));
                rs.absolute(5);
                assertEquals(13, rs.getInt("ID"));
                assertEquals("Nuraz Sukuranchy", rs.getString("Name"));
                assertEquals("123-45-67", rs.getString("Phone"));
            }
            {   //check table update
                assertEquals(Row.State.INTACT, tab.getRow(3).getState());
                assertEquals(12, tab.getRow(3).getField("ID"));
                assertEquals(Row.State.INTACT, tab.getRow(4).getState());
            }
        }
    }
    
    public void testUpdate() throws Exception{
        Table tab = getTestTable();
        {   //test update
            Row r = tab.getRow(0);
            r.setField("ID", 5);
            r.setField("Name", "Sikura Niemi");
            r.setField("Phone", "622-78-98");
            m_adapter.update(getConnection(), tab, getTabDef(), getDbName());
            {
                CachedRowSet rs = getCurrentRS();
                assertEquals(3, rs.size());
                rs.absolute(3);
                assertEquals(5, rs.getInt("ID"));
                assertEquals("Sikura Niemi", rs.getString("Name"));
                assertEquals("622-78-98", rs.getString("Phone"));
            }
            {
                assertEquals(Row.State.INTACT, tab.getRow(0).getState());
                assertEquals("Sikura Niemi", tab.getRow(0).getOriginalField("Name"));
            }
        }
    }

    public void testDelete() throws Exception{
        Table tab = getTestTable();
        {   //test delete
            tab.getRow(0).delete();
            tab.getRow(2).delete();
            m_adapter.delete(getConnection(), tab, getTabDef(), getDbName());
            {
                CachedRowSet rs = getCurrentRS();
                assertEquals(1, rs.size());
                rs.absolute(1);
                assertEquals(1, rs.getInt("ID"));
            }
            {
                assertEquals(1, tab.getSize());
            }
        }
    }    

}
