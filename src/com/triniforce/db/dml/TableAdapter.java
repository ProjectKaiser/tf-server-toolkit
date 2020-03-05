/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */

package com.triniforce.db.dml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.dml.Table.Row;
import com.triniforce.utils.ApiAlgs;

/**
 * Class for read and write data from database
 */
public class TableAdapter {

    public static class WhereClause {
        public String m_whereStr;

        public int m_fNums[];

        public WhereClause(TableDef tabDef, List<FieldDef> tabFlds) {
            List<String> fNames = new ArrayList<String>();
            for (FieldDef f : tabFlds) {
                fNames.add(f.getName());
            }
            IndexDef pk = null, uniqueIdx = null, tmp;
            for (int i = 0; i < tabDef.getIndices().size(); i++) {
                tmp = tabDef.getIndices().getElement(i);
                if (tmp.getType().equals(IndexDef.TYPE.PRIMARY_KEY)
                        && fNames.containsAll(tmp.getColumns())) {
                    pk = tmp;
                    break;
                } else if (uniqueIdx == null && tmp.isUnique()
                        && fNames.containsAll(tmp.getColumns())) {
                    uniqueIdx = tmp;
                }
            }
            if (pk == null && uniqueIdx != null)
                pk = uniqueIdx;

            if (pk != null) {
                m_fNums = new int[pk.getColumns().size()];
                int j = 0;
                for (String fName : pk.getColumns()) {
                    int i;
                    for (i = 0; i < fNames.size(); i++)
                        if (fNames.get(i).equals(fName))
                            break;
                    m_fNums[j++] = i;
                }
            } else {
                m_fNums = new int[fNames.size()];
                int realSz = 0;
                for (int i = 0; i < m_fNums.length; i++) {
                    if (!tabFlds.get(i).getType().equals(ColumnType.BLOB))
                        m_fNums[realSz++] = i;
                }
                if (realSz != m_fNums.length) {
                    int buf[] = new int[realSz];
                    for (int j = 0; j < buf.length; j++) {
                        buf[j] = m_fNums[j];
                    }
                    m_fNums = buf;
                }
            }
            for (int i = 0; i < m_fNums.length; i++) {
                if (i == 0)
                    m_whereStr = MessageFormat.format(
                            "{0}=?", fNames.get(m_fNums[i])); //$NON-NLS-1$
                else
                    m_whereStr += MessageFormat.format(
                            " AND {0}=?", fNames.get(m_fNums[i])); //$NON-NLS-1$
            }
        }

        public String get(Table.Row r) {
            return m_whereStr;
        }
    }

    private final String DELETE_CLAUSE = "DELETE FROM {0} WHERE {1}"; //$NON-NLS-1$

    private final String UPDATE_CLAUSE = "UPDATE {0} SET {1} WHERE {2}"; //$NON-NLS-1$

    private final String INSERT_CLAUSE = "INSERT INTO {0} ({1}) VALUES ({2})"; //$NON-NLS-1$

    /**
     * Load datatable
     * 
     * @param tab -
     *            table with own structure
     * @param rs -
     *            data stored in base
     * @throws SQLException
     */
    public void load(Table tab, ResultSet rs) throws SQLException {
        tab.populate(rs);
    }

    /**
     * Write data back
     * 
     * @param conn -
     *            connection with database
     * @param tab -
     *            table to be written
     * @param tabDef -
     *            full table definitio
     * @param dbName -
     *            database table name
     * @throws SQLException
     */
    public void flush(Connection conn, Table tab, TableDef tabDef, String dbName)
            throws SQLException {
        WhereClause where = new WhereClause(tabDef, tab.getFieldDefs());
        PreparedStatement delStmnt = null, updStmnt = null, insStmnt = null;
        for (int i = 0; i < tab.getSize(); i++) {
            Row r = tab.getRow(i);
            switch (r.getState()) {
            case INSERTED:
                if (insStmnt == null)
                    insStmnt = conn.prepareStatement(getInsertPattern(tab,
                            dbName));
                for (int j = 0; j < tab.getFieldDefs().size(); j++)
                    insStmnt.setObject(j + 1, r.getField(j), FieldDef
                            .sqlType(tab.getFieldDefs().get(j).getType()));
                insStmnt.addBatch();
                break;
            case UPDATED:
                if (updStmnt == null)
                    updStmnt = conn.prepareStatement(getUpdatePattern(tab,
                            dbName, where.m_whereStr));
                for (int j = 0; j < tab.getFieldDefs().size(); j++) {
                    updStmnt.setObject(j + 1, r.getField(j), FieldDef
                            .sqlType(tab.getFieldDefs().get(j).getType()));
                }
                for (int j = 0; j < where.m_fNums.length; j++) {
                    updStmnt.setObject(tab.getFieldDefs().size() + j + 1, r
                            .getOriginalField(where.m_fNums[j]), FieldDef
                            .sqlType(tab.getFieldDefs().get(where.m_fNums[j])
                                    .getType()));
                }
                updStmnt.addBatch();
                break;
            case DELETED:
                if (delStmnt == null)
                    delStmnt = conn.prepareStatement(getDeletePattern(tab,
                            dbName, where.m_whereStr));
                for (int j = 0; j < where.m_fNums.length; j++) {
                    delStmnt.setObject(j + 1, r
                            .getOriginalField(where.m_fNums[j]), FieldDef
                            .sqlType(tab.getFieldDefs().get(where.m_fNums[j])
                                    .getType()));
                }
                delStmnt.addBatch();
                break;
            case CANCELED:
            	break;
            case INTACT:
            	break;            	
            }
        }
        if (delStmnt != null)
            delStmnt.executeBatch();
        if (updStmnt != null)
            updStmnt.executeBatch();
        if (insStmnt != null)
            insStmnt.executeBatch();

        tab.acceptChanges();
    }

    public String getDeletePattern(Table tab, String dbName, String where) {
        return MessageFormat.format(DELETE_CLAUSE, dbName, where);
    }

    public String getUpdatePattern(Table tab, String dbName, String where) {
        String setV = null;
        List<FieldDef> flds = tab.getFieldDefs();
        int i;
        for (i = 0; i < flds.size(); i++) {
            if (i == 0)
                setV = MessageFormat.format(
                        "{0}=?", flds.get(i).getName().toUpperCase(Locale.ENGLISH)); //$NON-NLS-1$
            else
                setV += MessageFormat.format(
                        ",{0}=?", flds.get(i).getName().toUpperCase(Locale.ENGLISH)); //$NON-NLS-1$
        }
        return MessageFormat.format(UPDATE_CLAUSE, dbName, setV, where);
    }

    public String getInsertPattern(Table tab, String dbName) {
        String colNames = null;
        String colVals = null;
        List<FieldDef> flds = tab.getFieldDefs();
        int cols[] = new int[flds.size()];
        for (int i = 0; i < flds.size(); i++) {
            if (i == 0) {
                colNames = flds.get(i).getName().toUpperCase(Locale.ENGLISH);
                colVals = "?"; //$NON-NLS-1$
            } else {
                colNames += "," + flds.get(i).getName().toUpperCase(Locale.ENGLISH); //$NON-NLS-1$
                colVals += ",?"; //$NON-NLS-1$
            }
            cols[i] = i;
        }
        return MessageFormat.format(INSERT_CLAUSE, dbName, colNames, colVals);
    }

    private static abstract class SelectiveUpdate {
        public SelectiveUpdate(Connection conn, Table tab, Row.State state) {
            try {
                ArrayList<Integer> modified = new ArrayList<Integer>();
                PreparedStatement stmnt = null;
                try {
                    for (int i = 0; i < tab.getSize(); i++) {
                        Row row = tab.getRow(i);
                        if (row.getState().equals(state)) {
                            if (stmnt == null)
                                stmnt = conn.prepareStatement(getPattern());

                            fillStatement(stmnt, row);

                            stmnt.addBatch();
                            modified.add(i);
                        }
                    }
                    if (stmnt != null) {
                        stmnt.executeBatch();
                        tab.acceptRows(modified);
                    }
                } finally {
                    if (stmnt != null) {
                        stmnt.close();
                    }
                }

            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
        }

        public abstract String getPattern();

        public abstract void fillStatement(PreparedStatement stmnt, Row row)
                throws SQLException;
    }

    public void insert(Connection conn, final Table tab, TableDef tabDef,
            final String dbName) {

        new SelectiveUpdate(conn, tab, Row.State.INSERTED) {
            @Override
            public String getPattern() {
                return getInsertPattern(tab, dbName);
            }

            @Override
            public void fillStatement(PreparedStatement stmnt, Row row)
                    throws SQLException {
                for (int j = 0; j < tab.getFieldDefs().size(); j++){
                    int sqlType = FieldDef.sqlType(tab.getFieldDefs().get(j).getType());
                    Object value = row.getField(j);
                    stmnt.setObject(j + 1, value, sqlType);
                }
            }
        };

    }

    public void update(Connection conn, final Table tab, TableDef tabDef,
            final String dbName){
        final WhereClause where = new WhereClause(tabDef, tab.getFieldDefs());
        new SelectiveUpdate(conn, tab, Row.State.UPDATED) {
            @Override
            public String getPattern() {
                return getUpdatePattern(tab, dbName, where.m_whereStr);
            }

            @Override
            public void fillStatement(PreparedStatement stmnt, Row row)
                    throws SQLException {
                for (int j = 0; j < tab.getFieldDefs().size(); j++) {
                    stmnt.setObject(j + 1, row.getField(j), FieldDef
                            .sqlType(tab.getFieldDefs().get(j).getType()));
                }
                for (int j = 0; j < where.m_fNums.length; j++) {
                    stmnt.setObject(tab.getFieldDefs().size() + j + 1, row
                            .getOriginalField(where.m_fNums[j]), FieldDef
                            .sqlType(tab.getFieldDefs().get(where.m_fNums[j])
                                    .getType()));
                }
            }
        };
    }

    public void delete(Connection conn, final Table tab, TableDef tabDef,
            final String dbName) {
        final WhereClause where = new WhereClause(tabDef, tab.getFieldDefs());

        new SelectiveUpdate(conn, tab, Row.State.DELETED) {
            @Override
            public String getPattern() {
                return getDeletePattern(tab, dbName, where.m_whereStr);
            }

            @Override
            public void fillStatement(PreparedStatement stmnt, Row row)
                    throws SQLException {
                for (int j = 0; j < where.m_fNums.length; j++) {
                    stmnt.setObject(j + 1, row
                            .getOriginalField(where.m_fNums[j]), FieldDef
                            .sqlType(tab.getFieldDefs().get(where.m_fNums[j])
                                    .getType()));
                }
            }
        };
    }

}
