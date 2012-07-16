/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;

import java.sql.Statement;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IProfilerStack.PSI;

public class Stmt {
    protected final Statement m_statement;
    private final IStmtContainer m_parent;
    protected boolean m_closed = false;

    public Stmt(IStmtContainer parent, Statement stmt) {
        m_parent = parent;
        m_statement = stmt;
    }

    public void close() {
        try {
            m_statement.close();
            m_parent.detachStatement(this);
            m_closed = true;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

    @Deprecated
    public ResSet executeQuery(String sql){
        return executeQuery(sql, this.getClass().getName());
    }
    
    @Deprecated
    public boolean execute(String sql){
        return execute(sql, this.getClass().getName());
    }

    
    public ResSet executeQuery(String sql, String profItem) {
    	PSI psi = ApiAlgs.getProfItem(Stmt.class.getName(), profItem);
        try {
            return new ResSet(m_statement.executeQuery(sql));
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        finally{
        	ApiAlgs.closeProfItem(psi);
        }
        return null;
    }

    public boolean execute(String sql, String profItem) {
    	PSI psi = ApiAlgs.getProfItem(Stmt.class.getName(), sql);
        try {
            return m_statement.execute(sql);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        finally{
        	ApiAlgs.closeProfItem(psi);
        }
        return false;
    }

    public void addBatch(String sql) {
        try {
            m_statement.addBatch(sql);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

    public void executeBatch() {
    	PSI psi = ApiAlgs.getProfItem(Stmt.class.getName(), "Batch"); //$NON-NLS-1$
        try {
            m_statement.executeBatch();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        finally{
        	ApiAlgs.closeProfItem(psi);
        }
    }

    /**
     * @return the statement
     */
    public Statement getStatement() {
        return m_statement;
    }

    public int getResultSetConcurrency() {
        try {
            return m_statement.getResultSetConcurrency();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return 0;
    }

    public int getResultSetType() {
        try {
            return m_statement.getResultSetType();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return 0;
    }

    public boolean isClosed() {
        return m_closed;
    }    
    
}
