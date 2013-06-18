/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.dml.Table;
import com.triniforce.db.dml.TableAdapter;
import com.triniforce.db.dml.Table.Row;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IEntity;

public class EntityJournal<T extends IEntity> extends TableDef {

    public static final String ENTITY_NAME_FIELD = "entity_name"; //$NON-NLS-1$
    private static final String GET_ENTITY_NAMES_CMD = "select entity_name from {0}"; //$NON-NLS-1$

    public EntityJournal(String entityName) throws EDBObjectException {
        this(entityName, getDbTypeFromPool());
    }
    
    static DbType getDbTypeFromPool(){
        DbType dbType;
        Connection conn = SrvApiAlgs2.getPooledConnection();
        try{
        	dbType = UpgradeRunner.getDbType(conn);
        } finally{
        	SrvApiAlgs2.returnPooledConnection(ApiStack.getApi(), conn);
        }
        return dbType;
    }
    
    public EntityJournal(String entityName, DbType dbType) throws EDBObjectException {
        super(entityName);
        
    	ColumnType strType;
    	int strSize;
    	if(DbType.FIREBIRD.equals(dbType)){
    		strType = ColumnType.VARCHAR;
    		strSize = 250;
    	}
    	else{
    		strType = ColumnType.NVARCHAR;
    		strSize = IEntity.MAX_ENTITY_NAME;    		
    	}
    	
        addStringField(1, ENTITY_NAME_FIELD, strType, strSize, true, ""); //$NON-NLS-1$
        addIndex(2, "pk", new String[]{ENTITY_NAME_FIELD}, true, true); //$NON-NLS-1$
        deleteIndex(3, "pk");
        addPrimaryKey(4, "pkn", new String[]{ENTITY_NAME_FIELD});
    }

    public void add(Connection conn, String dbName, List<T> entities) throws SQLException {
        Table tab = new Table();
        for(int i=0; i<getFields().size(); i++)
            tab.addColumn(getFields().getElement(i));
        for (T entity : entities) {            
            Row row = tab.newRow();
            setRow(row, entity);
        }
        new TableAdapter().insert(conn, tab, this, dbName);
    }
    
    protected void setRow(Row row, T entity) {
        row.setField(ENTITY_NAME_FIELD, entity.getEntityName());        
    }

	public List<T> exclude(Connection conn, String dbName, List<T> entities)
			throws SQLException {
		ArrayList<T> res = new ArrayList<T>();
		Set<String> actual = getActual(conn, dbName);
		for (T def : entities) {
			if (!actual.contains(def.getEntityName()))
				res.add(def);
		}
		return res;
	}

	public Set<String> getActual(Connection conn, String dbName) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            ps = conn.prepareStatement(MessageFormat.format(GET_ENTITY_NAMES_CMD, dbName));
            rs = ps.executeQuery();
            HashSet<String> actual = new HashSet<String>();
            while(rs.next()){
                actual.add(rs.getString(ENTITY_NAME_FIELD));
            }     
            return actual;
        }finally{
            if(ps!=null){
                if(rs!=null)
                    rs.close();
                ps.close();
            }
        }        
	}
    
    
}
