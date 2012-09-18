/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.srvapi;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;

public interface IIdDef {
	FieldDef getFieldDef();
	FieldDef getFieldDef(IName name);
	FieldDef getFieldDef(IName name, boolean bNotNull);
	public static class Helper{
		public static FieldDef getFieldDef(){
			IIdDef idDef = ApiStack.getInterface(IIdDef.class);
			return idDef.getFieldDef();
		}
		public static FieldDef getFieldDef(IName name){
			IIdDef idDef = ApiStack.getInterface(IIdDef.class);
			return idDef.getFieldDef(name);
		}		
		public static FieldDef getFieldDef(IName name, boolean bNotNull){
			IIdDef idDef = ApiStack.getInterface(IIdDef.class);
			return idDef.getFieldDef(name, bNotNull);
		}
        public static FieldDef getParentRef(boolean bNotNull){
            return getFieldDef(QSelect.PARENT_REF_COLUMN, bNotNull);
        }		
		
		public static FieldDef getFieldDef(String fname){
			IIdDef idDef = ApiStack.getInterface(IIdDef.class);
			return idDef.getFieldDef(new ApiAlgs.SimpleName(fname));
		}		
		public static FieldDef getFieldDef(String fname, boolean bNotNull){
			IIdDef idDef = ApiStack.getInterface(IIdDef.class);
			return idDef.getFieldDef(new ApiAlgs.SimpleName(fname), bNotNull);
		}		
				
	}
}