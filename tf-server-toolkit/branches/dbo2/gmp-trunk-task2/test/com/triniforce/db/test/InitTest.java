/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InitTest extends DBTestCase {

	@Override
	public void test() throws Exception {
		Connection con = getConnection();
		
		Statement st = con.createStatement();
		try{
			st.execute("drop table testInit");
			con.commit();
		} catch(SQLException e){}
		
		
		st.execute("create table testInit (column1 integer)");
		con.commit();
		st.execute("insert into testInit (column1) values(12)");
		ResultSet rs = st.executeQuery("select \"COLUMN1\" from testInit");
		
		assertTrue(rs.next());
		assertEquals(12, rs.getInt(1));
		
	}
}
