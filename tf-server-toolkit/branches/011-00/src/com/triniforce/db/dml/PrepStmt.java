/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;

import java.io.InputStream;
import java.sql.PreparedStatement;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IProfilerStack.PSI;

public class PrepStmt extends Stmt {
    private String m_sqlItem;

	public PrepStmt(IStmtContainer parent, PreparedStatement stmt, String sqlItem) {
        super(parent, stmt);
        m_sqlItem = sqlItem;
    }

    protected PreparedStatement ps() {
        return (PreparedStatement) m_statement;
    }

    public void execute() {
    	
    	PSI psi = ApiAlgs.getProfItem(PrepStmt.class.getName(), m_sqlItem);
        try {
            ps().execute();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        finally{
        	ApiAlgs.closeProfItem(psi);
        }

    }

    public void setLong(int parameterIndex, long x) {
        try {
            ps().setLong(parameterIndex, x);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }        
    }
    
    public void setObject(int parameterIndex, Object x) {
        try {
            ps().setObject(parameterIndex, x);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }        
    }    
    
    /**
     * @param parameterIndex
     * @param SqlType Unfortunately for DERBY specify parameter type is a MUST !
     * Use constant from java.SqlTypes here.
     */
    public void setNull(int parameterIndex, int SqlType) {
        try {
            ps().setNull(parameterIndex, SqlType);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }        
    }    
    
    public void setBinaryStream(int parameterIndex, InputStream x, int length)  {
        try {
            ps().setBinaryStream(parameterIndex, x, length);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }        
    }  
    
    public ResSet executeQuery(){
    	PSI psi = ApiAlgs.getProfItem(PrepStmt.class.getName(), m_sqlItem);
        try {
            return new ResSet(ps().executeQuery());
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }      
        finally{
        	ApiAlgs.closeProfItem(psi);
        }
        return null;
    }
    
    @Override
    public PreparedStatement getStatement() {
        return (PreparedStatement) super.getStatement();
    }

    public void setNullable(int i, FieldDef fDef, Object value) {
        if(value == null)
            setNull(i, FieldDef.sqlType(fDef.getType()));
        else
            setObject(i, value);
    }
    
    public void addBatch(){
        try {
            ps().addBatch();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }      
    }
}
