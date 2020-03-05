/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.DBTestCase;

public class DDLTestCase extends DBTestCase {
        
    protected  boolean containTable(String tabName) throws Exception{
        ResultSet rs = getConnection().getMetaData().getTables(null, null, tabName.toUpperCase(Locale.ENGLISH), null);
        return rs.next();
    }
    
    protected  boolean containField(String tabName, FieldDef f) throws Exception{
		ResultSet rs = getConnection().getMetaData().getColumns(null, null, tabName.toUpperCase(Locale.ENGLISH), null);
		boolean bFound=false;
		while(rs.next()){
			String fName = rs.getString("COLUMN_NAME");
			if(fName.toUpperCase(Locale.ENGLISH).equals(f.m_name.toUpperCase(Locale.ENGLISH))){			
				bFound = f.m_type.equals(FieldDef.ddlType(rs.getInt("DATA_TYPE")));
				bFound &= f.m_size == rs.getInt("COLUMN_SIZE");
				bFound &= f.m_scale == rs.getInt("DECIMAL_DIGITS");
				bFound &= f.m_bNotNull == rs.getString("IS_NULLABLE").equals("NO");
			}
		}
        return bFound;
    }       
 
    protected  FieldDef getField(String tabName, String fname) throws Exception{
		ResultSet rs = getConnection().getMetaData().getColumns(null, null, tabName.toUpperCase(Locale.ENGLISH), fname.toUpperCase(Locale.ENGLISH));
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
		ResultSet rs = getConnection().getMetaData().getPrimaryKeys(null, null, tabName.toUpperCase(Locale.ENGLISH));
		boolean bFound=false;
		ArrayList<String> cols = new ArrayList<String>();
		while(rs.next()){
			if(pk.toUpperCase(Locale.ENGLISH).equals(rs.getString("PK_NAME").toUpperCase(Locale.ENGLISH))){
				if(!bFound)
					bFound = true;
				cols.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ENGLISH));
			}
		}
        return bFound ? IndexDef.primaryKey(pk, cols) : null;
    }   
    
    protected IndexDef getForeignKey(String tabName, String fk) throws SQLException, Exception{
		ResultSet rs = getConnection().getMetaData().getImportedKeys(null, null, tabName.toUpperCase(Locale.ENGLISH));
		boolean bFound=false;
		String pktab=null, fktab=null, fkcol=null, pkname=null, fkname=null;
		ArrayList<String> cols = new ArrayList<String>();
		while(rs.next()){
			fktab  = rs.getString("FKTABLE_NAME").toUpperCase(Locale.ENGLISH);
			fkcol  = rs.getString("FKCOLUMN_NAME").toUpperCase(Locale.ENGLISH);
			fkname = rs.getString("FK_NAME");
            if(fkname!=null) fkname = fkname.toUpperCase(Locale.ENGLISH);
			if(tabName.toUpperCase(Locale.ENGLISH).equals(fktab) && fk.toUpperCase(Locale.ENGLISH).equals(fkname)){
				if(!bFound){
					bFound = true;
					pktab  = rs.getString("PKTABLE_NAME").toUpperCase(Locale.ENGLISH);
					pkname = rs.getString("PK_NAME");
                    if(pkname!=null) pkname = pkname.toUpperCase(Locale.ENGLISH);
				}
				cols.add(fkcol);
			}
		}		
		return bFound ? IndexDef.foreignKey(fkname, cols, pktab, pkname) : null;    	
    }
    
    
}
