/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.BusinessLogic;
import com.triniforce.db.dml.ResSet;
import com.triniforce.utils.IName;

public class DBOVersion extends TableDef {
	public static final FieldDef fkey = FieldDef.createStringField("fkey", ColumnType.VARCHAR, 255, true, null);
	public static final FieldDef version = FieldDef.createScalarField("version", ColumnType.INT, true);
	
	public DBOVersion() {
		addField(1, fkey);
		addField(2, version);
		addPrimaryKey(3, "pk", new String[]{fkey.getName()});
	}
	
	public static class DBVersionBL extends BusinessLogic{
		@Override
		public Class getTable() {
			return DBOVersion.class;
		}
		
		public Map<String, Integer> getMap(){
			ResSet rs = select(new IName[]{fkey, version}, new IName[]{}, new Object[]{});
			HashMap<String, Integer> res = new HashMap<String, Integer>();
			while(rs.next()){
				res.put(rs.getString(1), rs.getInt(2));
			}
			return res;
		}
		
		public void update(boolean bExists, String key, int value){
			if(bExists){
				update(new IName[]{version}, new Object[]{value}, new IName[]{fkey}, new Object[]{key});
			}else{
				insert(new IName[]{fkey, version}, new Object[]{key, value});
			}
		}
		
	}
}
