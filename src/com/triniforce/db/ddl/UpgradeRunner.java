/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.IndexDef.TYPE;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IProfilerStack.PSI;

/**
 * Run operation sequence in database
 */
public class UpgradeRunner {

    public enum DbType {
        MSSQL, DERBY, MYSQL, OTHER, ORACLE, FIREBIRD, HSQL, H2
    };

    private DbType m_dbType;

    public boolean m_bSupportDropColumn;

    private String[] TYPE_CONVERTER;

    private static final String[] MSSQL_TYPES = {
            "INT", "SMALLINT", "REAL", "DATETIME", "DECIMAL", "CHAR", "NCHAR", "VARCHAR", "NVARCHAR", "IMAGE", "BIGINT", "FLOAT" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$

    private static final String[] DERBY_TYPES = {
            "INTEGER", "SMALLINT", "REAL", "TIMESTAMP", "DECIMAL", "CHAR", "CHAR", "VARCHAR", "VARCHAR", "BLOB", "BIGINT", "DOUBLE" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$

    private static final String[] ORACLE_TYPES = {
            "INTEGER", "SMALLINT", "FLOAT", "TIMESTAMP", "DECIMAL", "CHAR", "NCHAR", "VARCHAR2", "NVARCHAR2", "BLOB" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

    private static final String[] MYSQL_TYPES = {
            "INTEGER", "SMALLINT", "FLOAT", "TIMESTAMP", "DECIMAL", "CHAR", "CHAR", "VARCHAR", "VARCHAR", "LONGBLOB", "BIGINT", "DOUBLE" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$        

    private static final String[] FIREBIRD_TYPES = {
        	"INTEGER", "SMALLINT", "FLOAT", "TIMESTAMP", "DECIMAL", "CHAR", "CHAR", "VARCHAR", "VARCHAR", "BLOB", "BIGINT", "DOUBLE PRECISION" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$    
    
    public static final String ACT_STATE_TABLE =  ActualStateBL.ACT_STATE_TABLE;


    public interface IActualState {

        /**
         * Get database table name by application name
         * 
         * @param appName -
         *            table name in application
         * @return - database name
         */
        public String getDBName(String appName);

        /**
         * Get version of table
         * 
         * @param appName -
         *            application table name
         * @return - table version, if no such table then return 0
         * @throws SQLException
         */
        public int getVersion(String appName);

        /**
         * Generate name for new table
         * 
         * @param appTabName -
         *            application table name for we make database name
         * @return - new database name
         * @throws SQLException - ?
         *             something wrong in SQL
         */
        public String generateTabName(String appTabName) throws SQLException ;
        

        /**
         * Is table in database
         * 
         * @param appTableName -
         *            application table name
         * @return - true if is
         * @throws SQLException
         */
        public boolean tableExists(String appTableName) throws SQLException ;

        /**
         * Add table in actual state
         * 
         * @param dbName -
         *            database name
         * @param tabName -
         *            application table name
         * @param v -
         *            start version
         */
        public void addTable(String dbName, String tabName, int v);

        /**
         * Remove record from actual state
         * 
         * @param tabName -
         *            application table name
         */
        public void removeTable(String tabName);

        /**
         * Change stored version on dv(+ or -)
         * 
         * @param tabName -
         *            application table name
         * @param dv -
         *            version += dv
         */
        public void changeVersion(String tabName, int dv) ;

        /**
         * Get versions of several tables
         * 
         * @param tabs -
         *            list of table names
         * @return - map<table name, version>
         * @throws SQLException
         */
        public HashMap<String, Integer> getVersionMap(Collection<String> tabs)
                throws SQLException ;

        public String getAppName(String dbName) throws SQLException ;
        
        public String generateIndexName(String appName);
        public void addIndexName(String appName, String dbName);
        public String getIndexDbName(String appName);
		public String queryIndexName(String dbTabName);
        public void deleteIndexName(String appName);
        
//        public void loadState(HashMap<String, Integer> vers,
  //              HashMap<String, String> dbNames) throws SQLException ;
        
        /**
         * Flush changes generated previus actions in DB
         */
        void flush(Connection conn) throws SQLException ;

    }

    private IActualState m_actualState;

    private Connection m_connection;

	private String m_quateString;

    public UpgradeRunner(Connection conn, IActualState as) throws SQLException {
        m_connection = conn;
        m_actualState = as;

        m_dbType = getDbType(m_connection);
        
        m_quateString = m_connection.getMetaData().getIdentifierQuoteString();

        switch (m_dbType) {
        case MSSQL:
            TYPE_CONVERTER = MSSQL_TYPES;
            break;
        case ORACLE:
            TYPE_CONVERTER = ORACLE_TYPES;
            break;
        case MYSQL:
            TYPE_CONVERTER = MYSQL_TYPES;
            break;
        case FIREBIRD:
        	TYPE_CONVERTER = FIREBIRD_TYPES;
            break;
        default:
            TYPE_CONVERTER = DERBY_TYPES;
        }

        // m_bSupportDropColumn =
        // m_connection.getMetaData().supportsAlterTableWithDropColumn();
        m_bSupportDropColumn = true;//m_dbType != DbType.DERBY;
    }

    /**
     * init database configuration for first run
     * 
     * @throws SQLException
     * @throws EDBObjectException
     */
    public void init() throws SQLException, EDBObjectException {
/*        int vAS;
        ResultSet rs = null;
        try {
            DatabaseMetaData md = m_connection.getMetaData();
            rs = md.getTables(m_connection.getCatalog(), null, ACT_STATE_TABLE,
                    new String[] { "TABLE" }); //$NON-NLS-1$
            vAS = rs.next() ? m_actualState.getVersion(ACT_STATE_TABLE) : 0;

            if (vAS < 5) {
                HashMap<String, TableDef> desiredTables = new HashMap<String, TableDef>();
                TableDef tab = new TableDef(ACT_STATE_TABLE);
                tab.addModification(1, new AddColumnOperation(FieldDef
                        .createStringField(DBNAME, ColumnType.VARCHAR, 255,
                                true, null)));
                tab.addModification(2, new AddColumnOperation(FieldDef
                        .createStringField(APPNAME, ColumnType.VARCHAR, 255,
                                true, null)));
                tab
                        .addModification(3, new AddColumnOperation(FieldDef
                                .createScalarField(VERSION, ColumnType.INT,
                                        true, "0"))); //$NON-NLS-1$
                tab.addModification(4, new AddPrimaryKeyOperation(
                        "PK1", Arrays.asList(APPNAME))); //$NON-NLS-1$
                tab
                        .addModification(
                                5,
                                new AddIndexOperation(
                                        IndexDef
                                                .createIndex(
                                                        "PK2", Arrays.asList(DBNAME), true, true))); //$NON-NLS-1$

                desiredTables.put(tab.getEntityName(), tab);
                HashMap<String, Integer> actTabs = new HashMap<String, Integer>();
                actTabs.put(ACT_STATE_TABLE, vAS);

                DBTables tabs = new DBTables(actTabs, desiredTables);
                List<DBOperation> cl = tabs.getCommandList();
                if (cl.get(0).getOperation() instanceof CreateTableOperation) {
                    CreateTableOperation createOp = (CreateTableOperation) cl
                            .get(0).getOperation();
                    String sql = getCreateOperationString(ACT_STATE_TABLE,
                            createOp);
                    m_connection.prepareStatement(sql).execute();
                    m_actualState.addTable(m_connection, ACT_STATE_TABLE, ACT_STATE_TABLE,
                            createOp.getVersionIncrease());
                    cl.remove(0);
                }
                run(cl);
            }
        } finally {
            if (rs != null)
                rs.close();
        }*/
    }

    /**
     * Convert create operation to SQL command
     * 
     * @param dbName -
     *            generated database table name
     * @param createOp -
     *            create operation
     * @return - SQL command
     * @throws SQLException
     */
    public String getCreateOperationString(String dbName,
            CreateTableOperation createOp) throws SQLException {
        String sql = MessageFormat.format("CREATE TABLE {0} (", dbName); //$NON-NLS-1$

        ArrayList<TableUpdateOperation> elements = new ArrayList<TableUpdateOperation>(createOp.getElements().size());
        //remove all EmptyCommands
        Iterator<TableUpdateOperation> iter = createOp.getElements().iterator();
        while(iter.hasNext()){
        	TableUpdateOperation cmd = iter.next();
        	if(cmd instanceof EmptyCommand)
        		continue;
        	elements.add(cmd);
        }
        
        iter = elements.iterator();
        boolean condition = iter.hasNext();
        while (condition) {
            TableUpdateOperation node = iter.next();
            if (node instanceof AddColumnOperation)
                sql += getFieldDefString(((AddColumnOperation) node).getField());
            else if (node instanceof AddPrimaryKeyOperation)
                sql += getPrimaryKeyConstraintString(dbName,
                        (AddPrimaryKeyOperation) node, false);
            else if (node instanceof AddForeignKeyOperation)
                sql += getForeignKeyConstraintString(dbName,
                        (AddForeignKeyOperation) node, false);
            condition = iter.hasNext();
            if (condition)
                sql += ", "; //$NON-NLS-1$
        }
        sql += ")"; //$NON-NLS-1$
        if (m_dbType == DbType.MYSQL) {
            sql += " ENGINE = INNODB"; //$NON-NLS-1$
        }

        return sql;
    }

    private void runDropColumn(String tabName, DeleteColumnOperation delCol)
            throws SQLException {
        String defCnstr = null;
        if (delCol.getDeletedField() == null
                || delCol.getDeletedField().m_defaultValue != null) {
            defCnstr = getDefaultConstraintName(tabName, delCol.getName());
            if (defCnstr != null) {
                DBOperation dropDefCnstr = new DBOperation(tabName,
                        new DeleteDefaultConstraintOperation(defCnstr, delCol
                                .getName(),
                                delCol.getDeletedField().m_defaultValue));
                m_connection.prepareStatement(getOperationString(dropDefCnstr))
                        .execute();
                try {
                    m_connection
                            .prepareStatement(
                                    getOperationString(new DBOperation(tabName,
                                            delCol))).execute();
                } catch (SQLException e) {
                    m_connection.prepareStatement(
                            getOperationString(new DBOperation(tabName,
                                    dropDefCnstr.getOperation()
                                            .getReverseOperation()))).execute();
                    throw e;
                }
                return;
            }
        }
        String sql = getOperationString(new DBOperation(tabName, delCol));
        ApiAlgs.getLog(this).info(sql);
        m_connection.prepareStatement(sql).execute();
    }

    private void runOperation(DBOperation dbOp, boolean bForward)
            throws SQLException {
        TableOperation op = dbOp.getOperation();
        if (op instanceof CreateTableOperation && bForward) {
            CreateTableOperation createOp = (CreateTableOperation) op;
            String dbName = createOp.getDbName();
            if(null == dbName)
            	dbName = m_actualState.generateTabName(dbOp.getDBOName());
            String sql = getCreateOperationString(dbName, createOp);
            ApiAlgs.getLog(this).info(dbName + ": "+sql);//$NON-NLS-1$
//            try{
                m_connection.prepareStatement(sql).execute();
//            } catch (SQLException e){
            	
//            	ApiAlgs.rethrowException(e);
//            }
            m_actualState.addTable(dbName, dbOp.getDBOName(), createOp
                    .getVersionIncrease());
        } else {
            if (op instanceof DeleteColumnOperation && !m_bSupportDropColumn) {
                DeleteColumnOperation delOp = (DeleteColumnOperation) op;
                FieldDef oldField = delOp.getDeletedField();
                FieldDef newField = oldField.clone();
                newField.m_bNotNull = false;
                dbOp = new DBOperation(dbOp.getDBOName(),
                        new AlterColumnOperation(oldField, newField));
            }
            if (op instanceof DeleteColumnOperation && m_bSupportDropColumn) {
                runDropColumn(dbOp.getDBOName(), (DeleteColumnOperation) op);
            } else if (op instanceof SetAutoIncFieldOperation){
            	if(DbType.FIREBIRD.equals(m_dbType)){
            		SetAutoIncFieldOperation setInc = (SetAutoIncFieldOperation) op;
            		Statement st = m_connection.createStatement();
            		st.execute(MessageFormat.format("CREATE GENERATOR {0};", setInc.getGeneratorName()));
            		try{
            			st.execute(MessageFormat.format("SET GENERATOR {0} TO 0;", setInc.getGeneratorName()));
                		st.execute(MessageFormat.format(
	            				"CREATE TRIGGER T1 FOR {0} " +
				        		"BEFORE INSERT POSITION 0 " +
				        		"AS " +
				        		"BEGIN " +
				        		"if (NEW.{1} is NULL) then NEW.{1} = GEN_ID({2}, 1);" +
				        		"END", m_actualState.getDBName(dbOp.getDBOName()), setInc.getFieldName(), setInc.getGeneratorName()));
            		} catch(SQLException e){
            			st.execute("DROP GENERATOR gen_t1_id;");
            			ApiAlgs.rethrowException(e);
            		}
            	}
            } else if( DbType.MYSQL.equals(m_dbType) && 
            		op instanceof DeleteIndexOperation &&
            		IndexDef.TYPE.FOREIGN_KEY.equals(((DeleteIndexOperation)op).getType()) ){
            	DeleteIndexOperation delCnstr = (DeleteIndexOperation)op;
            	
                String dbName = m_actualState.getDBName(dbOp.getDBOName());
                String dbIndexName = m_actualState.getIndexDbName(getFullIndexName(dbName, delCnstr.getName(),
                		delCnstr.getType()));
            	
                String sql = getOperationString(dbOp);
                ApiAlgs.getLog(this).info(sql);//$NON-NLS-1$                
                m_connection.prepareStatement(sql).execute();
                
                try{
	                sql = MessageFormat.format("DROP INDEX {0} ON {1}", dbIndexName, dbName);
	                ApiAlgs.getLog(this).info(sql);//$NON-NLS-1$                
	                m_connection.createStatement().execute(sql);
                } catch(SQLException e){}
            }
            else if (op instanceof EmptyCommand){
            	ApiAlgs.getLog(this).trace("empty command");//$NON-NLS-1$
            }
            else{
            	
                String sql = getOperationString(dbOp);
                ApiAlgs.getLog(this).info(sql);//$NON-NLS-1$                
                m_connection.prepareStatement(sql).execute();
            }
            
            int vInc = op.getVersionIncrease();
            if (op instanceof DropTableOperation)// && !bForward)
                m_actualState.removeTable(dbOp.getDBOName());
            else
                m_actualState.changeVersion(dbOp.getDBOName(), bForward ? vInc
                        : -vInc);
        }
    }
    
	public TableOperation getReverseOperation(TableOperation op) {
        if (op instanceof DeleteColumnOperation && !m_bSupportDropColumn) {
            DeleteColumnOperation delOp = (DeleteColumnOperation) op;
            FieldDef newField = delOp.getDeletedField();
            FieldDef oldField = newField.clone();
            oldField.m_bNotNull = false;
            return new AlterColumnOperation(oldField, newField);
        }
        return op.getReverseOperation();
    }

    /**
     * Run operation sequence over database
     * 
     * @param cl -
     *            operation list
     * @throws SQLException
     */
    public void run(List<DBOperation> cl) throws SQLException {
        PSI psi = ApiAlgs.getProfItem("DB uprgade", "");
        boolean bAutoCommit = m_connection.getAutoCommit();
        m_connection.setAutoCommit(true);
        int opCnt = 0;
        try {
            for (ListIterator<DBOperation> iOp = cl.listIterator(); iOp
                    .hasNext();) {
                DBOperation operation = iOp.next();
                runOperation(operation, true);
                opCnt++;
            }
            m_actualState.flush(m_connection);
        } catch (SQLException eRun) {
            for (int i = opCnt - 1; i >= 0; i--) {
                try {
                    TableOperation op = getReverseOperation(cl.get(i)
                            .getOperation());
                    runOperation(new DBOperation(cl.get(i).getDBOName(), op),
                            false);
                } catch (Exception eRollback) {
                    eRollback.printStackTrace();
                }
            }
            throw eRun;
        } finally{
            m_connection.setAutoCommit(bAutoCommit);
            ApiAlgs.closeProfItem(psi);
        }
    }

    /**
     * Convert operations to SQL command, not use with CreateTableOperation
     * 
     * @param op -
     *            operation
     * @return - SQL - command
     * @throws SQLException
     */
    String getOperationString(DBOperation op) throws SQLException {
        String sql = null;
        String dbName = m_actualState.getDBName(op.getDBOName());
        if (op.getOperation() instanceof TableUpdateOperation) {
            if (op.getOperation() instanceof AddColumnOperation) {
                AddColumnOperation addCol = (AddColumnOperation) op
                        .getOperation();
                sql = MessageFormat
                        .format(
                                "ALTER TABLE {0} ADD {1}", dbName, getFieldDefString(addCol.getField())); //$NON-NLS-1$
            } else if (op.getOperation() instanceof AddPrimaryKeyOperation) {
                sql = MessageFormat.format("ALTER TABLE {0} ADD {1}", //$NON-NLS-1$
                        dbName, getPrimaryKeyConstraintString(dbName,
                                (AddPrimaryKeyOperation) op.getOperation(), false)); //$NON-NLS-1$
            } else if (op.getOperation() instanceof AddForeignKeyOperation) {
            	AddForeignKeyOperation addFK = (AddForeignKeyOperation) op.getOperation();
            	if(addFK.isCreateFK()){
	                sql = MessageFormat.format("ALTER TABLE {0} ADD {1}", //$NON-NLS-1$
	                        dbName, getForeignKeyConstraintString(dbName, addFK, false));
            	} 
            	else{
            		String dbIndexName = getDbIndexName(dbName, addFK, true);
            		sql = MessageFormat.format("CREATE INDEX {1} ON  {0} ({2})", //$NON-NLS-1$
	                        dbName, dbIndexName, colList(addFK.getIndex().getColumns()));
            	}
            } else if (op.getOperation() instanceof AddIndexOperation) {
                AddIndexOperation addIdx = (AddIndexOperation) op
                        .getOperation();
                String dbIndexName = getDbIndexName(dbName, addIdx, true);
                sql = getCreateIndexOperationString(addIdx.getColumns(), dbName, 
                		dbIndexName, addIdx.isUnique(), addIdx.isAscending(), addIdx.isClustered());
            } else if (op.getOperation() instanceof DeleteColumnOperation) {
                DeleteColumnOperation delCol = (DeleteColumnOperation) op
                        .getOperation();
                String dropColumnFmt = m_dbType.equals(DbType.FIREBIRD) ?
                		"ALTER TABLE {0} DROP {1}" : 
                		"ALTER TABLE {0} DROP COLUMN {1}";
                sql = MessageFormat
                        .format(dropColumnFmt, dbName, colList(Arrays.asList(delCol.getName()))); //$NON-NLS-1$
            } else if (op.getOperation() instanceof DeleteIndexOperation) {
                DeleteIndexOperation delIdx = (DeleteIndexOperation) op
                        .getOperation();
                String appIndexName = getFullIndexName(dbName, delIdx.getName(),
                        delIdx.getType()); 
                String dbIndexName = m_actualState.getIndexDbName(appIndexName);
                sql = getDeleteIndexOperationString(delIdx.getType(), dbName, dbIndexName, delIdx.isUniqueIndex());
                m_actualState.deleteIndexName(appIndexName);
            } else if (op.getOperation() instanceof AlterColumnOperation) {
                AlterColumnOperation alterCol = (AlterColumnOperation) op
                        .getOperation();
                FieldDef f = alterCol.getNewField();
                String alterColumnOp;
                if(m_dbType.equals(DbType.MYSQL))
                	alterColumnOp = "MODIFY";
                else
                	alterColumnOp = "ALTER";
                if(m_dbType.equals(DbType.DERBY)){
	                sql = MessageFormat
	                        .format(
	                                "ALTER TABLE {0} ALTER COLUMN {1} {2}", dbName, alterCol.getName(), //$NON-NLS-1$
	                                alterCol.bSetType() ? MessageFormat.format("SET DATA TYPE {0}", getTypeString(f.m_type, f.m_size, f.m_scale)) : 
	                                alterCol.bSetNotNullFlag() ? (f.m_bNotNull ? "NOT NULL" : "NULL") : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                	if(alterCol.bSetType()){
                		
                	}
                	else{
                		
                	}
                }
                else{
	                sql = MessageFormat
	                        .format(
	                                "ALTER TABLE {0} {4} COLUMN {1}{5}{2} {3}", dbName, alterCol.getName(), //$NON-NLS-1$
	                                alterCol.bSetType() || alterCol.bSetNotNullFlag() ? getTypeString(f.m_type,
	                                        f.m_size, f.m_scale) : "", //$NON-NLS-1$
	                                alterCol.bSetNotNullFlag() ? (f.m_bNotNull ? "NOT NULL" : "NULL") : "", alterColumnOp,
	                                m_dbType.equals(DbType.DERBY) ? " SET DATA TYPE " : " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
        } else if (op.getOperation() instanceof CreateTableOperation) {
            sql = null;
        } else if (op.getOperation() instanceof DropTableOperation) {
            sql = MessageFormat.format("DROP TABLE {0}", dbName); //$NON-NLS-1$
        } else if (op.getOperation() instanceof AddDefaultConstraintOperation) {
            AddDefaultConstraintOperation defCnstr = (AddDefaultConstraintOperation) op
                    .getOperation();
            sql = MessageFormat
                    .format(
                            "ALTER TABLE {0} ADD CONSTRAINT {1} DEFAULT ''{2}'' FOR {3}", dbName, defCnstr.getName(), defCnstr.getValue(), defCnstr.getColumnName()); //$NON-NLS-1$
        } else if (op.getOperation() instanceof DeleteDefaultConstraintOperation) {
            DeleteDefaultConstraintOperation delCnstr = (DeleteDefaultConstraintOperation) op
                    .getOperation();
            sql = MessageFormat
                    .format(
                            "ALTER TABLE {0} DROP CONSTRAINT {1}", dbName, delCnstr.getName()); //$NON-NLS-1$
        }         
        return sql;
    }

    public String getCreateIndexOperationString(List<String> cols, String dbTabName, 
    		String dbIndexName, boolean bUnique, boolean bAsc, boolean bClustered) {
        ArrayList<String> colSpec = new ArrayList<String>();
        for (String col : cols) {
            colSpec
                    .add(MessageFormat
                            .format(
                                    "{0} {1}", col, bAsc ? "ASC" : "DESC")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        String sql;
        if(bUnique){
        	sql = MessageFormat
            .format(
                    "ALTER TABLE {0} ADD CONSTRAINT {1} UNIQUE ({2})", //$NON-NLS-1$  
                    dbTabName, dbIndexName, colList(cols)); 
        }
        else{
            sql = MessageFormat
                    .format(
                            "CREATE {0} {4} INDEX {1} ON {2} ({3})", 
                            bUnique ? "UNIQUE" : "", dbIndexName, dbTabName, colList(cols),  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            m_dbType.equals(DbType.MSSQL) && bClustered ? "CLUSTERED" : "");
        }
		return sql;
	}

	public String getDeleteIndexOperationString(IndexDef.TYPE type, String dbTabName, String dbIndexName, boolean isUnique) {
        String sql;
		if (IndexDef.TYPE.INDEX.equals(type)) {
//        	if(EnumSet.of(DbType.DERBY,DbType.FIREBIRD, DbType.H2).contains(m_dbType)){
        		if(isUnique){
        			if(DbType.MYSQL.equals(m_dbType)){
	        			sql = MessageFormat.format(
	                            "ALTER TABLE {0} DROP INDEX {1}", dbTabName,  dbIndexName); //$NON-NLS-1$        				
        			}
        			else{
	        			sql = MessageFormat.format(
	                            "ALTER TABLE {0} DROP CONSTRAINT {1}", dbTabName,  dbIndexName); //$NON-NLS-1$
        			}
        		}
        		else{
        			if(DbType.MSSQL.equals(m_dbType) || DbType.MYSQL.equals(m_dbType)){
                		sql = MessageFormat.format(
                                "DROP INDEX {0} ON {1}", dbIndexName, dbTabName); //$NON-NLS-1$        				
        			}
        			else
	            		sql = MessageFormat.format(
	                            "DROP INDEX {0}", dbIndexName); //$NON-NLS-1$
        		}
//        	}
//        	else if(EnumSet.of(DbType.H2).contains(m_dbType)){
//        		sql = MessageFormat.format(
//                        "DROP INDEX {0}", dbIndexName); //$NON-NLS-1$                		
//        	}
//        	else{
//        		sql = MessageFormat.format(
//                    "DROP INDEX {0} ON {1}", dbIndexName, dbTabName); //$NON-NLS-1$
//        	}
        } else {
            if (m_dbType.equals(DbType.MYSQL)) {
                if (IndexDef.TYPE.PRIMARY_KEY.equals(type))
                    sql = MessageFormat.format(
                            "ALTER TABLE {0} DROP PRIMARY KEY", dbTabName); //$NON-NLS-1$
                else
                    sql = MessageFormat
                            .format(
                                    "ALTER TABLE {0} DROP FOREIGN KEY {1}", dbTabName, dbIndexName); //$NON-NLS-1$
            } else
                sql = MessageFormat
                        .format(
                                "ALTER TABLE {0} DROP CONSTRAINT {1}", dbTabName, dbIndexName); //$NON-NLS-1$
        }
		return sql;
	}

	/**
     * @return actual state
     */
    public IActualState getActualState() {
        return m_actualState;
    }

    public String colList(List<String> columns) {
        Iterator<String> iter = columns.iterator();
        boolean condition = iter.hasNext();
        String res = ""; //$NON-NLS-1$
        while (condition) {
            res += m_quateString + iter.next().toUpperCase() + m_quateString;
            condition = iter.hasNext();
            if (condition)
                res += ", "; //$NON-NLS-1$
        }
        return res;
    }

    public String getTypeString(ColumnType t, int size, int scale) {
        String res = TYPE_CONVERTER[t.ordinal()];
        if (FieldDef.isStringType(t)){
            res += MessageFormat.format("({0})", Integer.toString(size)); //$NON-NLS-1$
            if((t.equals(ColumnType.NCHAR) || t.equals(ColumnType.NVARCHAR))){
            	if(m_dbType.equals(DbType.MYSQL))
            		res += " character set utf8"; //$NON-NLS-1$
	        	if(m_dbType.equals(DbType.FIREBIRD))
	        		res += " character set UNICODE_FSS"; //$NON-NLS-1$
            }
            else{
	        	if(m_dbType.equals(DbType.FIREBIRD))
	        		res += " character set ASCII"; //$NON-NLS-1$            	
	        	if(m_dbType.equals(DbType.MYSQL))
	        		res += " character set ASCII"; //$NON-NLS-1$            	
            }
        }
        else if (FieldDef.isDecimalType(t))
            res += MessageFormat.format("({0},{1})", size, scale); //$NON-NLS-1$
        return res;
    }

    public String getFieldDefString(FieldDef f) {
    	
    	//if(!f.getName().toLowerCase().equals(f.getName()))
//    		throw new RuntimeException("must be lowercase: " + f.getName());
    	
        String res;
        res = m_quateString + f.m_name.toUpperCase() + m_quateString + " " + getTypeString(f.m_type, f.m_size, f.m_scale); //$NON-NLS-1$
        if(f.isAutoincrement()){
        	if(DbType.MSSQL.equals(m_dbType)){
        		res += " IDENTITY(1,1)";
        	}
        	else if(DbType.MYSQL.equals(m_dbType)){
        		res += " AUTO_INCREMENT";
        	}
        	else if(DbType.DERBY.equals(m_dbType)){
        		res += " GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";
        	}
        }
        if (f.m_defaultValue != null) {
            res += " DEFAULT "; //$NON-NLS-1$
            if (FieldDef.isStringType(f.getType()))
                res += MessageFormat.format("''{0}''", f.m_defaultValue); //$NON-NLS-1$
            else
                res += f.m_defaultValue.toString();
        }
        if (f.m_bNotNull)
            res += " NOT NULL"; //$NON-NLS-1$
        if(f.getType().equals(ColumnType.DATETIME) && m_dbType.equals(DbType.MYSQL))
            res += " NULL DEFAULT NULL"; //$NON-NLS-1$
        return res;
    }

    public String getPrimaryKeyConstraintString(String dbName,
            AddPrimaryKeyOperation addPk, boolean bExisting) {
    	
    	String dbIndexName;
    	if(m_dbType.equals(DbType.MYSQL)){
    		dbIndexName = "";
    	}
    	else{
    		dbIndexName = getDbIndexName(dbName, addPk, !bExisting);
    	}
    	
        return MessageFormat
                .format(
                        "CONSTRAINT {0} PRIMARY KEY ({1})", //$NON-NLS-1$
                        dbIndexName, colList(addPk.getIndex().getColumns())); //$NON-NLS-1$

    }

    private String getDbIndexName(String dbName, AddIndexOperation op, boolean bNew) {
    	String res;
    	IndexDef index = op.getIndex();
		String fullName = getFullIndexName(dbName, index.getName(), index.getType());
		if(!op.isDbIndexName()){
			if(bNew){
		    	res = m_actualState.generateIndexName(fullName);
		    	m_actualState.addIndexName(fullName, res);
			}
			else{
				res = m_actualState.getIndexDbName(fullName);
			}
		}
		else
			res = fullName;
		return res;
    }

	public String getForeignKeyConstraintString(String dbName,
            AddForeignKeyOperation addFk, boolean bExisting) throws SQLException {
    	String res;
    	String dbIndexName = getDbIndexName(dbName, addFk, !bExisting);
    	if(addFk.isCreateFK()){
    		String refTabName = m_actualState.getDBName(addFk.getParentTable());
        	res = MessageFormat
                .format(
                        "CONSTRAINT {0} FOREIGN KEY ({1}) REFERENCES {2} ({3}){4}{5}", //$NON-NLS-1$
                        dbIndexName,
                        colList(addFk.getIndex().getColumns()),
                        refTabName,
                        colList(addFk.getRefColumns()),
                        addFk.getOnDeleteRule() == AddForeignKeyOperation.DELETE_RULE.NOT_SPECIFIED ? "" : " ON DELETE CASCADE", //$NON-NLS-1$ //$NON-NLS-2$
                        addFk.getOnUpdateRule() == AddForeignKeyOperation.UPDATE_RULE.NOT_SPECIFIED ? "" : ""); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else{
    		res = MessageFormat
            .format(
                    "INDEX {0} ({1})", //$NON-NLS-1$
                    dbIndexName,
                    colList(addFk.getIndex().getColumns()));
    	}
    	return res;
    	
    }

    public String getFullIndexName(String dbTabName, String index, TYPE idxType) {
        String res = null;
        if (idxType.equals(IndexDef.TYPE.PRIMARY_KEY)
                && m_dbType.equals(DbType.MYSQL))
            res = "PRIMARY"; //$NON-NLS-1$
        else
            res = dbTabName + '_' + index;
        return res;
    }

    public static DbType getDbType(Connection conn) {
        DbType res = DbType.OTHER;
        String strDbType = null;

        try {
            strDbType = conn.getMetaData().getDatabaseProductName();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        if (strDbType.equals("Microsoft SQL Server")) //$NON-NLS-1$
            res = DbType.MSSQL;
        else if (strDbType.equals("Apache Derby")) //$NON-NLS-1$
            res = DbType.DERBY;
        else if (strDbType.equals("MySQL")) //$NON-NLS-1$
            res = DbType.MYSQL;
        else if (strDbType.equals("Oracle")) //$NON-NLS-1$
            res = DbType.ORACLE;
        else if (strDbType.startsWith("Firebird")) //$NON-NLS-1$
            res = DbType.FIREBIRD;
        else if (strDbType.startsWith("HSQL Database Engine")) //$NON-NLS-1$
            res = DbType.HSQL;
        else if (strDbType.startsWith("H2")) //$NON-NLS-1$
            res = DbType.H2;        
        return res;
    }

    public String getDefaultConstraintName(String tabName, String colName)
            throws SQLException {
        String res = null;
        if (m_dbType.equals(DbType.MSSQL)) {
            ResultSet rs = m_connection
                    .createStatement()
                    .executeQuery(
                            MessageFormat
                                    .format(
                                            "select def.name from dbo.syscolumns as col join dbo.sysobjects as tab on tab.id = col.id join dbo.sysobjects as def on def.id = col.cdefault where col.name = ''{0}'' and tab.name = ''{1}''", colName, m_actualState.getDBName(tabName))); //$NON-NLS-1$
            if (rs.next())
                res = rs.getString("name"); //$NON-NLS-1$
        }
        return res;
    }
}
