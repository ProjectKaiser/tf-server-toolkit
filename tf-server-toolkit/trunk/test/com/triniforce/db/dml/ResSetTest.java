package com.triniforce.db.dml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResSetTest extends DMLTestCase {
	
	public void testToString() throws SQLException, Exception {
		
		//
	    Statement stmnt = getConnection().createStatement();        
	    ResultSet rs = stmnt.executeQuery("SELECT * FROM "+getDbName()+" ORDER BY ID");
	    
	    ResSet resSet = new ResSet(rs);
	    String s = resSet.toString();
	    assertNotNull(s);
	    trace(s);
	    rs.close();
        stmnt.close();
	}

}
