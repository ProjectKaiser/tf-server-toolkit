/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.dml;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.triniforce.db.ddl.AddColumnOperation;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.DBTestCase;

/**
 *
 */
public class SmartTranTest extends DBTestCase {

    private String m_tabName;
    private SmartTran m_tr;

    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        m_tabName = this.createTableIfNeeded(new TableDef(
        "SmartTranTest.testTable").addModification(1,
        new AddColumnOperation(FieldDef.createScalarField("i",
                ColumnType.INT, false))));
        
        m_tr = new SmartTran(getConnection(), null);
    }

    public void testSmartTran() throws Exception{
        Stmt stmnt1 = m_tr.getStatement();
        stmnt1.execute("insert into "+m_tabName+" (i) values (647)", "test");
        Stmt stmnt2 = m_tr.getStatement();
        ResSet rs = stmnt2.executeQuery("select i from "+m_tabName, "test");
        assertTrue(rs.next());
        assertEquals(647, rs.getInt(1));
        m_tr.close();
    }
    
    /**
     * Test method for {@link com.triniforce.db.dml.SmartTran#commit()}.
     * @throws Exception 
     */
    public void testCommit() throws Exception {
        {
            Stmt st = m_tr.getStatement();
            st.execute("insert into "+m_tabName+" (i) values (6352)", "test");
            m_tr.commit();
            SmartTran tr2 = new SmartTran(getConnection(), null);
            Stmt st_check = tr2.getStatement();
            ResSet rs = st_check.executeQuery("select * from "+m_tabName+" where i=6352", "test");
            assertTrue(rs.next());
            tr2.commit();
            try{    //get statement from closed transaction
                m_tr.getStatement();
                fail();
            } catch ( Exception e){}
        }
        {
            m_tr.commit();
            assertTrue( m_tr.isCommited());

        }
    }

    /**
     * Test method for {@link com.triniforce.db.dml.SmartTran#end()}.
     * @throws Exception 
     */
    public void testEnd() throws Exception {
        {
            Stmt st = m_tr.getStatement();
            st.execute("insert into "+m_tabName+" (i) values (6352)", "test");
            m_tr.close();
            assertFalse(m_tr.isCommited());
            try{
                st.execute("insert into "+m_tabName+" (i) values (63534)", "test");
                fail();
            } catch(RuntimeException e){}
            
            SmartTran tr2 = new SmartTran(getConnection());
            Stmt st_check = tr2.getStatement();
            ResSet rs = st_check.executeQuery("select * from "+m_tabName+" where i=6352", "test");
            assertFalse(rs.next());
            tr2.commit();
            assertTrue(tr2.isCommited());            
            
        }
        {
            m_tr = new SmartTran(getConnection());
            Stmt st = m_tr.getStatement();
            st.execute("insert into "+m_tabName+" (i) values (6352)", "test");
            m_tr.commit();
            m_tr.close();
            SmartTran tr2 = new SmartTran(getConnection());
            Stmt st_check = tr2.getStatement();
            ResSet rs = st_check.executeQuery("select * from "+m_tabName+" where i=6352", "test");
            assertTrue(rs.next());
            tr2.commit();
        }
        {
            m_tr = new SmartTran(getConnection());
            m_tr.close();
            try{    //get statement from closed transaction
                m_tr.getStatement();
                fail();
            } catch (IStmtContainer.EContainerClosed e){}
        }
    }

    /**
     * Test method for {@link com.triniforce.db.dml.SmartTran#getStatement()}.
     * @throws SQLException 
     */
    public void testGetStatement() throws SQLException {
        Stmt st = m_tr.getStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        assertNotNull(st);
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, st.getResultSetType());
        assertEquals(ResultSet.CONCUR_READ_ONLY, st.getResultSetConcurrency());
    }

    /**
     * Test method for {@link com.triniforce.db.dml.SmartTran#closeStatement(java.sql.Statement)}.
     * @throws SQLException 
     */
    public void testReturnStatement() throws SQLException {
        Stmt st = m_tr.getStatement();
        m_tr.closeStatement(st);
        try{
            st.execute("insert into "+m_tabName+" (i) values (6352)", "test");
            fail();
        } catch(RuntimeException e){}
    }
    
    public void testDoNotCommit() throws Exception{
        Stmt st = m_tr.getStatement();
        st.execute("insert into "+m_tabName+" (i) values (6352)", "test");
        assertTrue(m_tr.toBeCommited());
        m_tr.doNotCommit();
        assertFalse(m_tr.toBeCommited());
        m_tr.commit();

        SmartTran tr2 = new SmartTran(getConnection());
        Stmt st_check = tr2.getStatement();
        ResSet rs = st_check.executeQuery("select * from "+m_tabName+" where i=6352", "test");
        assertFalse(rs.next());
    }

}
