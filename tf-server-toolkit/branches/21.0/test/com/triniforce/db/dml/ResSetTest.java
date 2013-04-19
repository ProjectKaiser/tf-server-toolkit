package com.triniforce.db.dml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResSetTest extends DMLTestCase {
	
	public void testToString() throws SQLException, Exception {
		
		//
		ResSet resSet = new ResSet(null);
	    assertTrue(resSet.toString().equals("\n"));
		
		//
	    Statement stmnt = getConnection().createStatement();        
	    ResultSet rs = stmnt.executeQuery("SELECT * FROM "+getDbName()+" ORDER BY ID");
	    
	    resSet = new ResSet(rs);
	    String s = resSet.toString();
	    assertNotNull(s);
	    trace(s);
	    rs.close();
        stmnt.close();
	}

}
