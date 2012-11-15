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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.triniforce.db.ddl.AddColumnOperation;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.test.DBTestCase;
import com.triniforce.server.plugins.kernel.SrvTable;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;
import com.triniforce.utils.IProfilerStack;
import com.triniforce.utils.Profiler;
import com.triniforce.utils.Profiler.INanoTimer;

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
    
    public static class TestDef extends TableDef{
    	public static  FieldDef f1 = FieldDef.createScalarField("f1", ColumnType.INT, true);
    	public static  FieldDef f2 = FieldDef.createStringField("f2", ColumnType.NVARCHAR, 64, true, null);
    	public static  FieldDef f3 = FieldDef.createScalarField("f3", ColumnType.INT, false);
    	
		public TestDef() {
        	addField(1, f1);
			addField(2, f2);
			addField(3, f3);
		}
    }

    public void testInsertUpdate() throws Exception{
    	createTableIfNeeded(new TestDef());
    	SmartTran tr = new SmartTran(getConnection());
    	String guid = UUID.randomUUID().toString();
    	String guid2 = UUID.randomUUID().toString();

    	//insert
    	{
	    	Map<IName, Object> values = new HashMap<IName, Object>();
	    	values.put(TestDef.f1, 1);
	    	values.put(TestDef.f2, guid);
	    	values.put(TestDef.f3, 3);
	    	tr.insert(TestDef.class, values);
	    	ResSet rs = tr.select(TestDef.class, new IName[]{TestDef.f1, TestDef.f2, TestDef.f3}, new IName[]{TestDef.f2},  new Object[]{guid});
	    	assertTrue(rs.next());
	    	assertEquals(1, rs.getObject(1));
	    	assertEquals(guid, rs.getObject(2));
	    	assertEquals(3, rs.getObject(3));
	    	assertFalse(rs.next());
    	}
    	
    	//insert2
    	{
	    	Map<IName, Object> values = new HashMap<IName, Object>();
	    	values.put(TestDef.f1, 1);
	    	values.put(TestDef.f2, guid2);
	    	values.put(TestDef.f3, 3);
	    	tr.insert(TestDef.class, values);
	    	ResSet rs = tr.select(TestDef.class, new IName[]{TestDef.f1, TestDef.f2, TestDef.f3}, new IName[]{TestDef.f2},  new Object[]{guid2});
	    	assertTrue(rs.next());
	    	assertEquals(1, rs.getObject(1));
	    	assertEquals(guid2, rs.getObject(2));
	    	assertEquals(3, rs.getObject(3));
	    	assertFalse(rs.next());
    	}
    	
    	//update
    	{
    		Map<IName, Object> values = new HashMap<IName, Object>();
    		values.put(TestDef.f1, 111);
    		Map<IName, Object> where = new HashMap<IName, Object>();
    		where.put(TestDef.f2, guid);
    		tr.update(TestDef.class, values, where);
    		ResSet rs = tr.select(TestDef.class, new IName[]{TestDef.f1, TestDef.f2, TestDef.f3}, new IName[]{TestDef.f2},  new Object[]{guid});
    		assertTrue(rs.next());
	    	assertEquals(111, rs.getObject(1));
	    	rs = tr.select(TestDef.class, new IName[]{TestDef.f1, TestDef.f2, TestDef.f3}, new IName[]{TestDef.f2},  new Object[]{guid2});
    		assertTrue(rs.next());
	    	assertEquals(1, rs.getObject(1));
    	}
    	
    	//update2
    	{
    		Map<IName, Object> values = new HashMap<IName, Object>();
    		values.put(TestDef.f1, 1111);
    		Map<IName, Object> where = new HashMap<IName, Object>();
    		where.put(TestDef.f2, guid2);
    		tr.update(TestDef.class, values, where);
    		ResSet rs = tr.select(TestDef.class, new IName[]{TestDef.f1, TestDef.f2, TestDef.f3}, new IName[]{TestDef.f2},  new Object[]{guid});
    		assertTrue(rs.next());
	    	assertEquals(111, rs.getObject(1));
	    	rs = tr.select(TestDef.class, new IName[]{TestDef.f1, TestDef.f2, TestDef.f3}, new IName[]{TestDef.f2},  new Object[]{guid2});
    		assertTrue(rs.next());
	    	assertEquals(1111, rs.getObject(1));
    	}
    	
    	
    }
    
    public void testSelectBetween() throws Exception{
        createTableIfNeeded(new TestDef());
        SmartTran tr2 = new SmartTran(getConnection());
        tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{1,"v_1", 100});
        tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{2,"v_2", 100});
        tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{3,"v_3", 101});
        ResSet res = tr2.select(TestDef.class
                , new IName[]{TestDef.f1}
                , new IName[]{TestDef.f1}, new Object[]{new ISmartTran.Between((Integer)1, (Integer)2)}
                , new IName[]{TestDef.f1}
        );
        assertTrue(res.next());
        assertEquals("1", res.getString(1));
        assertTrue(res.next());
        assertEquals("2", res.getString(1));
        assertFalse(res.next());
        
        res = tr2.select(TestDef.class
                , new IName[]{TestDef.f1}
                , new IName[]{TestDef.f1}, new Object[]{new ISmartTran.Between((Integer)2, (Integer)3)}
                , new IName[]{TestDef.f1}
        );
        assertTrue(res.next());
        assertEquals("2", res.getString(1));
        assertTrue(res.next());
        assertEquals("3", res.getString(1));
        assertFalse(res.next());        
        
    }

    
    public void testSelect() throws Exception{
    	createTableIfNeeded(new TestDef());
    	
        SmartTran tr2 = new SmartTran(getConnection());
        {
        	List<IName> fields = new ArrayList<IName>();
        	fields.add(TestDef.f1);
        	fields.add(TestDef.f2);
        	fields.add(TestDef.f3);
        	tr2.insert(TestDef.class, fields, Arrays.asList(new Object[]{1,"v_1", 100}));
        }
    	//tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{1,"v_1", 100});
    	tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{2,"v_2", 100});
    	tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{3,"v_3", 101});
    	
    	ResSet res = tr2.select(TestDef.class, new IName[]{TestDef.f2}, new IName[]{TestDef.f1}, new Object[]{2});
    	assertTrue(res.next());
    	assertEquals("v_2", res.getString(1));
    	assertFalse(res.next());
    	
    	res = tr2.select(TestDef.class, new IName[]{TestDef.f2}, new IName[]{TestDef.f1}, new Object[]{new Object[]{1,3}});
    	assertTrue(res.next());
    	assertEquals("v_1", res.getString(1));
    	assertTrue(res.next());
    	assertEquals("v_3", res.getString(1));
    	assertFalse(res.next());
    	
    	res = tr2.select(TestDef.class, new IName[]{TestDef.f2}, new IName[]{TestDef.f3, TestDef.f1}, new Object[]{100, new Object[]{1,3}});
    	assertTrue(res.next());
    	assertEquals("v_1", res.getString(1));
    	assertFalse(res.next());
    	
    	res = tr2.select(TestDef.class, new IName[]{TestDef.f2}, new IName[]{TestDef.f3}, new Object[]{null});
    	assertFalse(res.next());
    	
    	
    	res = tr2.select(TestDef.class, new IName[]{TestDef.f1}, new IName[]{}, new Object[]{}, 
    			new IName[]{new ISmartTran.DescName(TestDef.f1.getName())});
    	assertTrue(res.next());
    	assertEquals(3, res.getInt(1));
    	assertTrue(res.next());
    	assertEquals(2, res.getInt(1));
    	assertTrue(res.next());
    	assertEquals(1, res.getInt(1));
    	assertFalse(res.next());
    	
    }
    
    public void testPrepareStatement() throws Exception{
    	createTableIfNeeded(new TestDef());
    	
        SmartTran tr2 = new SmartTran(getConnection());
    	tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{1,"v_1", 100});
    	tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{2,"v_2", 100});
    	tr2.insert(TestDef.class, new IName[]{TestDef.f1,TestDef.f2,TestDef.f3}, new Object[]{3,"v_3", 101});
    	
    	PrepStmt ps = tr2.prepareStatement(new QSelect().joinLast(new SrvTable(TestDef.class).addCol(TestDef.f1)).toString(), "testPrepareStatement_profItem1");
    	Profiler pr = new Profiler();
    	ApiStack.pushInterface(IProfilerStack.class, new Profiler.ProfilerStack(pr, new INanoTimer() {
            public long get() {
                return System.nanoTime();
            }
        }));
    	try{
	    	ps.executeQuery();
	    	String res = pr.toString();
	    	assertTrue(res, res.contains("testPrepareStatement_profItem1"));
    	} finally{
    		ApiStack.popInterface(1);
    	}
    	
    }

}
