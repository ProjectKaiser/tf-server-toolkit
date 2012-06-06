/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;


import java.util.Arrays;

import junit.framework.TestCase;

import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.EUnknownReference;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.Fields;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.Indices;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

public class DBTableIndicesTest extends TestCase {

    private TableDef m_table;
    private Indices m_indices;
    private Fields m_fields;

    protected void setUp() throws Exception {
        super.setUp();
        m_table = new TableDef("table");        
        m_indices = m_table.getIndices();
        m_fields = m_table.getFields();
    }    

    public void testAddElementIndexDef() throws EMetadataException {
        {
            m_fields.addElement(FieldDef.createScalarField("col1", ColumnType.INT, true));
            IndexDef index1 = IndexDef.primaryKey("pk1", Arrays.asList("col1"));
            IndexDef index2 = IndexDef.foreignKey("fk1", Arrays.asList("col1"), "tab1", "pk1");
            m_indices.addElement(index1);
            m_indices.addElement(index2);
            assertEquals(m_indices.size(), 2);
            assertEquals(index1, m_indices.getElement(0));
            assertEquals(index2, m_indices.getElement(1));
        }
        {
            try{
                m_indices.addElement(IndexDef.foreignKey("fk2", Arrays.asList("unknown"), "tab2", "pk"));
                fail();
            } catch(EUnknownReference e){}
        }
        {
            m_indices.removeElement(0);
            m_fields.addElement(FieldDef.createScalarField("col2", ColumnType.INT, false));
            IndexDef index = IndexDef.primaryKey("pk12", Arrays.asList("col2"));
            try{
                m_indices.addElement(index);
                fail();
            } catch(EMetadataException e){}
        }
        {
            m_indices.addElement(IndexDef.primaryKey("pk13", Arrays.asList("col1")));
            m_fields.addElement(FieldDef.createScalarField("col3", ColumnType.INT, true));
            try{
                m_indices.addElement(IndexDef.primaryKey("pk2", Arrays.asList("col3")));
                fail("two primary key in table must raised");
            } catch(EMetadataException e){}            
        }
    }

}
