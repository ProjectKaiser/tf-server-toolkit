/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.sql.Types;

import junit.framework.TestCase;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.TableDef.FieldDef.EWrongVObjectType;
import com.triniforce.server.soap.NamedVar;
import com.triniforce.server.soap.VObject;

public class DBTableFieldDefTest extends TestCase {
      
    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.FieldDef.equals(Object)'
     */
    public void testEqualsObject() {
        assertFalse("different types", FieldDef.createScalarField("f_name", ColumnType.INT, true).equals(FieldDef.createStringField("f_name", ColumnType.CHAR, 245, true, "''")));
        assertFalse("different names", FieldDef.createScalarField("f_name", ColumnType.INT, true).equals(FieldDef.createScalarField("f_name2", ColumnType.INT, true)));
        assertTrue("equals names and types", FieldDef.createScalarField("f_name", ColumnType.INT, true).equals(FieldDef.createScalarField("f_name", ColumnType.INT, true)));
    }

    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.FieldDef.getName()'
     */
    public void testGetName() {
        FieldDef f = FieldDef.createScalarField("f_name", ColumnType.INT, true);
        assertEquals("f_name", f.getName());
    }

    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.FieldDef.simpleField(String, ColumnType, boolean)'
     */
    public void testSimpleField() {
        {
            FieldDef f = FieldDef.createScalarField("f_name", ColumnType.INT, true);
            assertEquals("f_name", f.getName());
            assertEquals(ColumnType.INT, f.m_type);
            assertEquals(true, f.m_bNotNull);
            assertEquals(1, f.m_size);
            assertEquals(0, f.m_scale);
            
            f = FieldDef.createScalarField("f_name", ColumnType.LONG, true);
            assertEquals(ColumnType.LONG, f.m_type);
        }
        {   // DOUBLE
            assertTrue(FieldDef.isScalarType(ColumnType.DOUBLE));
            assertTrue(FieldDef.isScalarType(ColumnType.FLOAT));
        }        
    }

    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.FieldDef.stringField(String, ColumnType, int, boolean)'
     */
    public void testStringField() {
        FieldDef f = FieldDef.createStringField("f_name2", ColumnType.CHAR, 245, true, "''");
        assertEquals("f_name2", f.getName());
        assertEquals(ColumnType.CHAR, f.m_type);
        assertEquals(true, f.m_bNotNull);
        assertEquals(245, f.m_size);
        assertEquals(0, f.m_scale);
    }

    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.FieldDef.decimalField(String, int, int, boolean)'
     */
    public void testDecimalField() {
        {
            FieldDef f = FieldDef.createDecimalField("f_name", 21, 12, false, "0.0");
            assertEquals("f_name", f.getName());
            assertEquals(ColumnType.DECIMAL, f.m_type);
            assertEquals(false, f.m_bNotNull);
            assertEquals(21, f.m_size);
            assertEquals(12, f.m_scale);
        }
        {
            try{
                FieldDef.createDecimalField("f_name", 21, -1, false, "0.0");
                fail();
            } catch(TableDef.FieldDef.EScaleLessThanZero e){}
        }
        {
            try{
                FieldDef.createDecimalField("f_name", 21, 34, false, "0.0");
                fail();
            } catch(TableDef.FieldDef.EPrecisionLessThanScale e){}
        }
    }
    
    public void testSqlType(){
        assertEquals(Types.DOUBLE, FieldDef.sqlType(ColumnType.DOUBLE));
    }

    public void testToVObject(){
        
        VObject nv = FieldDef.createDecimalField("f_name", 21, 12, false, "0.0").toVObject();
        assertNotNull(nv);        
        assertEquals(new VObject(FieldDef.class.getName(),
                new NamedVar[]{
                    new NamedVar("name", "f_name"),
                    new NamedVar("type", "DECIMAL"),
                    new NamedVar("size", 21), 
                    new NamedVar("scale", 12), 
                    new NamedVar("bMustHaveValue", 0), 
                    new NamedVar("defVal", "0.0")
                }), 
            nv);
        
        nv = FieldDef.createScalarField("sc_field", ColumnType.BLOB, true).toVObject();
        assertEquals(new VObject(FieldDef.class.getName(),
                new NamedVar[]{
                    new NamedVar("name", "sc_field"),
                    new NamedVar("type", "BLOB"), 
                    new NamedVar("bMustHaveValue", 1)
                }), nv);
    }

    public void testFromVObject(){
        {
            FieldDef res = FieldDef.fromVObject(
                    new VObject(FieldDef.class.getName(), 
                            new NamedVar[]{
                        new NamedVar("bMustHaveValue", 0),
                        new NamedVar("type", "NVARCHAR"), 
                        new NamedVar("size", 352),
                        new NamedVar("defVal", "gtjdksl"),
                        new NamedVar("name", "str_field")
                    }));
            
            assertEquals("str_field", res.getName());
            assertEquals(ColumnType.NVARCHAR, res.getType());
            assertEquals(352, res.getSize());
            assertEquals("gtjdksl", res.getDefaultValue());
            assertEquals(false, res.bNotNull());
            assertEquals(0, res.getScale());
        }
        {
            FieldDef res = FieldDef.fromVObject(
                    new VObject(FieldDef.class.getName(), 
                            new NamedVar[]{
                        new NamedVar("bMustHaveValue", 1),
                        new NamedVar("type", "INT"), 
                        new NamedVar("name", "int_field")
                    }));
            
            assertEquals("int_field", res.getName());
            assertEquals(ColumnType.INT, res.getType());
            assertEquals(null, res.getDefaultValue());
            assertEquals(true, res.bNotNull());
        }
        {
            try{
                FieldDef.fromVObject(
                        new VObject(FieldDef.class.getName(), 
                                new NamedVar[]{
                            new NamedVar("bMustHaveValue", 0),
                            new NamedVar("type", "DECIMAL"), 
                            new NamedVar("size", 352),
                            new NamedVar("defVal", "gtjdksl"),
                            new NamedVar("name", "str_field")
                        }));
                fail();
            } catch(VObject.EPropNotFound e){
                assertEquals("scale", e.getMessage());
            }
        }
        {
            try{
                FieldDef.fromVObject(
                        new VObject("wrong.type", 
                                new NamedVar[]{
                            new NamedVar("bMustHaveValue", 0),
                            new NamedVar("type", "DECIMAL"), 
                            new NamedVar("size", 352),
                            new NamedVar("defVal", "gtjdksl"),
                            new NamedVar("name", "str_field")
                        }));
                fail();
            } catch(EWrongVObjectType e){
                assertEquals("wrong.type", e.getMessage());
            }
        }
    }
}
