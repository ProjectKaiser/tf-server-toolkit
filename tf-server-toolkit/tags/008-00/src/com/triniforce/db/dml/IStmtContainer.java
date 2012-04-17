/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;


/**
 * Issues statement, keeps list of issued statements, close them all on self
 * close()
 */
public interface IStmtContainer {

    public static class EContainerClosed extends RuntimeException {
        private static final long serialVersionUID = 5143532292333107645L;

        public EContainerClosed(String msg) {
            super(msg);
        }
    }

       
    public static class ESQLProblem extends RuntimeException {
        private static final long serialVersionUID = 6249416544389071567L;

        public ESQLProblem(Throwable t) {
            super(t);
        }
    }

    /**
     * All <code>get..Statement()</code> methods returns statement and put
     * statement to internal list of returned statements.
     * <code>get...Statement()</code> returns statement which is valid only
     * within its own block of code. Therefore, returned statements can be
     * stored to LOCAL variables only, not to class members.
     * 
     * @throws RuntimeException
     * 
     */
    Stmt getStatement();

    Stmt getStatement(int resultSetType, int resultSetConcurrency);

    boolean isClosed();
    
    PrepStmt prepareStatement(String sql);
    PrepStmt prepareStatement(String sql, String profItem);

    PrepStmt prepareStatement(Class prepSql);

    PrepStmt prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency);

    PrepStmt prepareStatement(Class prepSql, int resultSetType,
            int resultSetConcurrency);
    /**
     * 
     * Closes and detaches statement
     * 
     * @param stmt
     * 
     */
    void closeStatement(Stmt stmt);

    /**
     * @param stmt
     *            Statement to detach from list
     */
    void detachStatement(Stmt stmt);

    /**
     * Update or delete
     */
    void execute(Class prepSql, Object... args);
    
    /**
     * Select
     */
    ResSet executeQuery(Class PrepSql, Object... args);    
    
    /**
     * Invokes close() for all child containers. Close all statements and close
     * container so no statements can be get anymore. Detach from parent
     * container, if any.
     */
    void close();

    void closeAllStatements();

    /**
     * @return number of statements
     */
    int countStmt();
    
    /**
     * @return number of child containers
     */
    int countChilds();    

    /**
     * Attaches new child container
     * @return attached container
     */
    IStmtContainer attachContainer(IStmtContainer child);

    void detachContainer(IStmtContainer child);
    
    /**
     * @return new container attached to this one
     */
    IStmtContainer newContainer();    

}
