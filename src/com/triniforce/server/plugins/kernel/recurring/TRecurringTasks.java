/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.recurring;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.dml.BusinessLogic;
import com.triniforce.db.dml.ResSet;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.utils.IName;

public class TRecurringTasks extends TableDef {
    
    /**
     * Id must be returned by IIdGenerator
     */
    public static final FieldDef f_id = IIdDef.Helper.getFieldDef("id");
    
    public static final FieldDef f_extension_id = IIdDef.Helper.getFieldDef("extension_id");
    
    /**
     * Start time, milliseconds 
     */
    public static final FieldDef f_start = FieldDef.createScalarField("start", FieldDef.ColumnType.LONG, true);
    
    /**
     * Start task even if start in the past but not more then given value
     * 
     */
    public static final FieldDef f_past_threshold = FieldDef.createScalarField("past_threshold", FieldDef.ColumnType.LONG, true);
    
    public static class Data{
        public long id;
        public long extension_id;
        public long start;
        public long past_threshold;
    }
    
    public TRecurringTasks() {
        addField(1, f_id);
        addField(2, f_extension_id);
        addField(3, f_start);
        addField(4, f_past_threshold);
        addIndex(5, "start_idx", new String[]{f_start.getName()}, false, true);
        addIndex(6, "id_idx", new String[]{f_id.getName()}, true, true);
        deleteIndex(7, "id_idx");
        addIndex(8, "id_extension_idx", new String[]{f_id.getName(), f_extension_id.getName()}, true, true);
//        addPrimaryKey(9, "pk", new String[]{f_id.getName(), f_extension_id.getName()});
//        deleteIndex(10, "id_extension_idx");
    }
    
    public static class BL extends BusinessLogic{

        @Override
        public Class getTable() {
            return TRecurringTasks.class;
        }

        public void insert(long id, long extension_id, long start, long past_threshold){
            insert(new IName[]{f_id, f_extension_id, f_start, f_past_threshold}, new Object[]{id, extension_id, start, past_threshold});
        }
        
        public Data selectFirst(){
            ResSet rs = select(new IName[]{f_id, f_extension_id, f_start, f_past_threshold}, new IName[]{}, new Object[]{}, new IName[]{f_start});
            if(!rs.next()) return null;
            Data res = new Data();
            res.id = rs.getLong(1);
            res.extension_id = rs.getLong(2);
            res.start = rs.getLong(3);
            res.past_threshold = rs.getLong(4);
            return res;
        }
        
        @Deprecated
        public void delete(long id){
            delete(new IName[]{f_id}, new Object[]{id});
        }
        
        public void delete(long id, long extensionId){
            delete(new IName[]{f_id, f_extension_id}, new Object[]{id, extensionId});
        }
        
        public void deleteAll(){
            delete(new IName[]{}, new Object[]{});
        }
        
    }
}
