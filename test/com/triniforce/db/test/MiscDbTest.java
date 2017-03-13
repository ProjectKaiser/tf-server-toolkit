/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.db.test;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.framework.AssertionFailedError;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.qbuilder.Expr;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QTable;
import com.triniforce.db.qbuilder.WhereClause;

/**
 * Test issues with keys
 * 
 */
public class MiscDbTest extends DBTestCase {

    protected String tableName;    
 
    public void testUpperCase() throws Exception {
        String tabName = this.createTableIfNeeded(
                new TableDef("KeysTest.testUpperCase")
                  .addScalarField(1, "ser", ColumnType.INT, true, null)
                  .addStringField(2, "name", ColumnType.NVARCHAR, 100, true, null)
         );
        {//Insert 2, mAx
            PreparedStatement stmt = getConnection().prepareStatement("insert into " + tabName + " ( ser, name) values(?,?)");
            stmt.setInt(1, 1);
            stmt.setString(2, "MAx");
            stmt.execute();
            stmt.setInt(1, 1);
            stmt.setString(2, "MAx2");
            stmt.execute();
            stmt.setInt(1, 1);
            stmt.setString(2, "MAx3");
            stmt.execute();            
            stmt.setInt(1, 2);
            stmt.setString(2, "vIC");
            stmt.execute();            
            stmt.close();
        }
        getConnection().commit();
        
        QSelect sel = new QSelect().joinLast(new QTable(tabName, "t").addCol("name"));
        WhereClause where = new WhereClause();
        where.or(
                new Expr.Compare(
                        new Expr.Func(Expr.Funcs.Upper, new Expr.Column("t", "name"))
                        ,Expr.EqKind.EQ
                        ,new Expr.Param()
                )
        );
        sel.where(where);
        System.out.println(sel);
        
        {//select {fn Upper("max")}       
        	trace(sel.toString());
            PreparedStatement stmt = getConnection().prepareStatement(sel.toString());
            stmt.setString(1, "max".toUpperCase());
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("MAx", rs.getString(1));
            assertFalse(rs.next());            
            stmt.close();            
        }
        {//select {fn Upper("vIC")}
            PreparedStatement stmt = getConnection().prepareStatement(sel.toString());
            stmt.setString(1, "vic".toUpperCase());
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("vIC", rs.getString(1));
            assertFalse(rs.next());            
            stmt.close();            
        }
        
    }
    
    public void testSelfReference() throws Exception {      
        String tabName = this.createTableIfNeeded(
                new TableDef("KeysTest.testSelfReference")
                  .addScalarField(1, "ser", ColumnType.INT, true, null)
                  .addPrimaryKey(2, "ser_pk", new String[]{"ser"})
                  .addScalarField(3, "ref", ColumnType.INT, false, null)
                  .addForeignKey(4, "ref_fk", new String[]{"ref"}, "KeysTest.testSelfReference", "ser_pk", false) 
         );
        
        {//2 2 ok
            PreparedStatement stmt = getConnection().prepareStatement("insert into " + tabName + " ( ser, ref) values(2,2) ");
            stmt.execute();
            stmt.close();
        }

        //2 2 can not be inserted
        {
            PreparedStatement stmt = getConnection().prepareStatement("insert into " + tabName + " ( ser, ref) values(2,2) ");
            try{
                stmt.execute();
                assertTrue(false);
            }
            catch(Throwable t){
                if ( t instanceof AssertionFailedError ){
                    throw new Exception();
                }
            }
            stmt.close();        
        }
        
        // 2 3 can not
        {
            PreparedStatement stmt = getConnection().prepareStatement("insert into " + tabName + " ( ser, ref) values(2,3) ");
            try{
                stmt.execute();
                assertTrue(false);
            }
            catch(Throwable t){
                if ( t instanceof AssertionFailedError ){
                    throw new Exception();
                }
            }
            stmt.close();            
        }
        
        // 5 2 can
        {
            PreparedStatement stmt = getConnection().prepareStatement("insert into " + tabName + " ( ser, ref) values(5,2) ");
            stmt.execute();
            stmt.close();            
        }        
        
        // 4 4 can
        {
            PreparedStatement stmt = getConnection().prepareStatement("insert into " + tabName + " ( ser, ref) values(4,4) ");
            stmt.execute();
            stmt.close();            
        }        

        // 5 5 can not ( 5 already exists )
        {
            PreparedStatement stmt = getConnection().prepareStatement("insert into " + tabName + " ( ser, ref) values(5,5) ");
            try{
                stmt.execute();
                assertTrue(false);
            }
            catch(Throwable t){
                if ( t instanceof AssertionFailedError ){
                    throw new Exception();
                }
            }
            stmt.close();            
        }
        
        getConnection().commit();

        // ser <> ref can be deleted with no problem
        {
            PreparedStatement stmt = getConnection().prepareStatement("delete from " + tabName + " where ser <> ref");
            stmt.execute();
            stmt.close();            
        }
        
        // still there is record(s)
        {
            PreparedStatement stmt = getConnection().prepareStatement("select count(*) from " + tabName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            assertTrue( rs.getInt(1) > 0 );          
        }
        
        {//delete self reference
            PreparedStatement stmt = getConnection().prepareStatement("update " + tabName + " set ref = null");
            stmt.execute();
            stmt.close();
            stmt = getConnection().prepareStatement("delete from " + tabName);
            stmt.execute();
            stmt.close();         
        }
        // no records anymore
        {
            PreparedStatement stmt = getConnection().prepareStatement("select count(*) from " + tabName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            assertEquals( rs.getInt(1), 0 );          
        }                        
        
        getConnection().commit();        
    }

}
