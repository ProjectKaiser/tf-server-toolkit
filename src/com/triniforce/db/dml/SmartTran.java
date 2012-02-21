/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.dml;

import java.sql.Connection;

import com.triniforce.utils.ApiAlgs;

/**
 * @author Alex
 * 
 */
public class SmartTran extends StmtContainer implements ISmartTran {

    private boolean m_bCommited;
    private boolean m_doNotCommit = false;

    /**
     * Construct transaction Connection.autoCommit must be false
     * 
     * @param connection
     */
    public SmartTran(Connection connection, IPrepSqlGetter sqlGetter) {
        super(connection, sqlGetter);
        m_bCommited = false;
    }

    public SmartTran(Connection connection) {
        this(connection, null);
    }

    public void commit() {
        close(toBeCommited());
    }

    public void close(boolean bCommit) {
        if (m_closed)
            return;

        //close all statements first
        super.close();
        
        try {
            if(bCommit){
                m_conn.commit();                
            }else{
                m_conn.rollback();
            }

        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        m_bCommited = bCommit;
    }

    @Override
    public void close() {
        close(false);

    }

    public boolean isCommited() {
        return m_bCommited;
    }

    public void doNotCommit() {
        m_doNotCommit = true;
    }

    public boolean toBeCommited() {
        return !m_doNotCommit;
    }

}
