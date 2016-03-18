/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.DBTestCase;

public class DDLTestCase extends DBTestCase {
        
    protected  boolean containTable(String tabName) throws Exception{
        ResultSet rs = getConnection().getMetaData().getTables(null, null, tabName.toUpperCase(), null);
        return rs.next();
    }
    
    protected  boolean containField(String tabName, FieldDef f) throws Exception{
		ResultSet rs = getConnection().getMetaData().getColumns(null, null, tabName.toUpperCase(), null);
		boolean bFound=false;
		while(rs.next()){
			String fName = rs.getString("COLUMN_NAME");
			if(fName.toUpperCase().equals(f.m_name.toUpperCase())){			
				bFound = f.m_type.equals(FieldDef.ddlType(rs.getInt("DATA_TYPE")));
				bFound &= f.m_size == rs.getInt("COLUMN_SIZE");
				bFound &= f.m_scale == rs.getInt("DECIMAL_DIGITS");
				bFound &= f.m_bNotNull == rs.getString("IS_NULLABLE").equals("NO");
			}
		}
        return bFound;
    }       
 
    protected  FieldDef getField(String tabName, String fname) throws Exception{
		ResultSet rs = getConnection().getMetaData().getColumns(null, null, tabName.toUpperCase(), fname.toUpperCase());
		FieldDef f=null;
		if(rs.next()){
			String fName = rs.getString("COLUMN_NAME");
			ColumnType t= FieldDef.ddlType(rs.getInt("DATA_TYPE"));
			boolean bNotNull = rs.getString("IS_NULLABLE").equals("NO");
			switch(t){
            case INT:
            case LONG:
            case SMALLINT:
            case FLOAT:
            case DOUBLE:
            case DATETIME:
            case BLOB:
            	f = FieldDef.createScalarField(fName, t, bNotNull);
                break;
            case DECIMAL:
                int scale;
                int size = rs.getInt("COLUMN_SIZE");
				scale = rs.getInt("DECIMAL_DIGITS");
                f = FieldDef.createDecimalField(fName, size, scale, bNotNull, "0.0");
                break;
            case CHAR:
            case NCHAR:
            case VARCHAR:
            case NVARCHAR:
				size = rs.getInt("COLUMN_SIZE");
                f = FieldDef.createStringField(fName, t, size, bNotNull, "''");
                break;
            }
		}
        return f;
    }
    
    protected  IndexDef getPrimaryKey(String tabName, String pk) throws Exception{
//        if(getDbType().equals(DbType.MYSQL))
//            pk = "PRIMARY";
		ResultSet rs = getConnection().getMetaData().getPrimaryKeys(null, null, tabName.toUpperCase());
		boolean bFound=false;
		ArrayList<String> cols = new ArrayList<String>();
		while(rs.next()){
			if(pk.toUpperCase().equals(rs.getString("PK_NAME").toUpperCase())){
				if(!bFound)
					bFound = true;
				cols.add(rs.getString("COLUMN_NAME").toLowerCase());
			}
		}
        return bFound ? IndexDef.primaryKey(pk, cols) : null;
    }   
    
    protected IndexDef getForeignKey(String tabName, String fk) throws SQLException, Exception{
		ResultSet rs = getConnection().getMetaData().getImportedKeys(null, null, tabName.toUpperCase());
		boolean bFound=false;
		String pktab=null, fktab=null, fkcol=null, pkname=null, fkname=null;
		ArrayList<String> cols = new ArrayList<String>();
		while(rs.next()){
			fktab  = rs.getString("FKTABLE_NAME").toUpperCase();
			fkcol  = rs.getString("FKCOLUMN_NAME").toUpperCase();
			fkname = rs.getString("FK_NAME");
            if(fkname!=null) fkname = fkname.toUpperCase();
			if(tabName.toUpperCase().equals(fktab) && fk.toUpperCase().equals(fkname)){
				if(!bFound){
					bFound = true;
					pktab  = rs.getString("PKTABLE_NAME").toUpperCase();
					pkname = rs.getString("PK_NAME");
                    if(pkname!=null) pkname = pkname.toUpperCase();
				}
				cols.add(fkcol);
			}
		}		
		return bFound ? IndexDef.foreignKey(fkname, cols, pktab, pkname) : null;    	
    }
    
    
}
