/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IProfilerStack.PSI;

public class StmtContainer implements IStmtContainer {

    protected Connection m_conn;

    HashSet<Stmt> m_statements = new HashSet<Stmt>();

    HashSet<IStmtContainer> m_childs;

    boolean m_closed;

    private final IPrepSqlGetter m_sqlGetter;

    private final IStmtContainer m_parent;

    public void close() {
        if( m_closed) return;
        {// detach child containers
            if (null != m_childs && m_childs.size() > 0) {
                for (IStmtContainer child : m_childs
                        .toArray(new IStmtContainer[m_childs.size()])) {
                    try {
                        child.close();
                    } catch (Exception e) {
                        ApiAlgs.getLog(this).error("", e); //$NON-NLS-1$
                    }
                }
            }
        }
        closeAllStatements();
        m_closed = true;
        if( null != m_parent){
            m_parent.detachContainer(this);
        }
    }

    public void checkClosed() {
        if (m_closed) {
            throw new EContainerClosed(this.getClass().getName());
        }
    }

    public StmtContainer(IStmtContainer parent, Connection connection,
            IPrepSqlGetter sqlGetter) {
        m_parent = parent;
        if( null != parent ){
            parent.attachContainer(this);
        }
        m_conn = connection;
        m_sqlGetter = sqlGetter;
    }

    public StmtContainer(Connection connection, IPrepSqlGetter sqlGetter) {
        this(null, connection, sqlGetter);
    }

    public Stmt getStatement() {
        checkClosed();
        try {
            Stmt stmnt = new Stmt(this, m_conn.createStatement());
            m_statements.add(stmnt);
            return stmnt;
        } catch (SQLException e) {
            throw new IStmtContainer.ESQLProblem(e);
        }
    }

    public Stmt getStatement(int resultSetType, int resultSetConcurrency) {
        checkClosed();
        try {
            Statement stmnt = m_conn.createStatement(resultSetType,
                    resultSetConcurrency);
            Stmt stmt = new Stmt(this, stmnt);
            m_statements.add(stmt);
            return stmt;
        } catch (SQLException e) {
            throw new IStmtContainer.ESQLProblem(e);
        }
    }

    public void closeAllStatements() {
        checkClosed();
        for (Stmt stmnt : m_statements.toArray(new Stmt[m_statements.size()])) {
            try {
                stmnt.close();
            } catch (Exception e) {
                ApiAlgs.getLog(this).error("", e); //$NON-NLS-1$
            }
        }
    }

    public static final String PROF_PREPARE = "prepare";
    
    public PrepStmt prepareStatement(String sql) {
    	return prepareStatement(sql, sql);
    }
    
    public PrepStmt prepareStatement(String sql, String profItem) {
        PSI psi = ApiAlgs.getProfItem(PrepStmt.class.getName(), PROF_PREPARE);
        ApiAlgs.getLog(StmtContainer.class).trace(sql);
        try {
            checkClosed();
            PreparedStatement stmnt = m_conn.prepareStatement(sql);
            PrepStmt stmt = new PrepStmt(this, stmnt, profItem);
            m_statements.add(stmt);
            return stmt;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        finally{
            ApiAlgs.closeProfItem(psi);
        }
        return null;
    }

    public PrepStmt prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) {
        
        PSI psi = ApiAlgs.getProfItem(PrepStmt.class.getName(), PROF_PREPARE);
        ApiAlgs.getLog(StmtContainer.class).trace(sql);
        try {
            checkClosed();
            PreparedStatement stmnt = m_conn.prepareStatement(sql,
                    resultSetType, resultSetConcurrency);
            PrepStmt stmt = new PrepStmt(this, stmnt, sql);
            m_statements.add(stmt);
            return stmt;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        finally{
            ApiAlgs.closeProfItem(psi);
        }
        return null;

    }

    public void closeStatement(Stmt stmnt) {
        stmnt.close();
    }

    public void detachStatement(Stmt stmt) {
        m_statements.remove(stmt);

    }

    public PrepStmt prepareStatement(Class prepSql) {
        try {
            checkClosed();
            String sql = m_sqlGetter.getSql(prepSql);
            PSI psi = ApiAlgs.getProfItem(PrepStmt.class.getName(), PROF_PREPARE);
            ApiAlgs.getLog(StmtContainer.class).trace(prepSql.getName());
            PreparedStatement stmnt;
            try {
                stmnt = m_conn.prepareStatement(sql);
            } finally{
                ApiAlgs.closeProfItem(psi);
            }
            PrepStmt stmt = new PrepStmt(this, stmnt, prepSql.getName());
            m_statements.add(stmt);
            return stmt;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }

        return null;
    }
    
    public PrepStmt prepareStatement(Class prepSql, int resultSetType,
            int resultSetConcurrency) {

        try {
            checkClosed();
            String sql = m_sqlGetter.getSql(prepSql);;
            PrepStmt stmt = prepareStatement(sql,
                    resultSetType, resultSetConcurrency);
            return stmt;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;

    }

    public int countStmt() {
        return m_statements.size();
    }

    public IStmtContainer attachContainer(IStmtContainer child) {
        if( null == m_childs){
            m_childs= new HashSet<IStmtContainer>();
        }
        m_childs.add(child);
        return child;
    }

    public void detachContainer(IStmtContainer child) {
        m_childs.remove(child);
    }

    public int countChilds() {
        if( null == m_childs){
            return 0;
        }
        return m_childs.size();
    }
    
    public IStmtContainer newContainer(){
        return new StmtContainer(this, m_conn, m_sqlGetter); 
    }

    public void execute(Class prepSql, Object... args) {
        PrepStmt ps = this.prepareStatement(prepSql);
        int ind = 0;
        for( Object obj:args){
            ind++;
            ps.setObject(ind, obj);
        }
        ps.execute();        
    }

    public ResSet executeQuery(Class prepSql, Object... args) {
        PrepStmt ps = this.prepareStatement(prepSql);
        int ind = 0;
        for( Object obj:args){
            ind++;
            ps.setObject(ind, obj);
        }
        return ps.executeQuery();
    }

    public boolean isClosed() {
        return m_closed;
    }

}
