/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.triniforce.utils.ApiAlgs;

public class ResSet extends BasicResSet implements IResSet {
    protected final ResultSet m_resultSet;

    public ResultSet getResultSet() {
        return m_resultSet;
    }

    public ResSet(ResultSet resultSet) {
        m_resultSet = resultSet;
    }

    public boolean next() {
        try {
            return m_resultSet.next();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }

        return false;
    }

    public Object getObject(int columnIndex) {
        try {
            return m_resultSet.getObject(columnIndex);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

    public long getLong(int columnIndex) {
        try {
            return m_resultSet.getLong(columnIndex);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return 0;
    }    
    public String getString(int columnIndex) {
        try {
            return m_resultSet.getString(columnIndex);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return ""; //$NON-NLS-1$
    }
    
    public int getInt(int columnIndex) {
        try {
            return m_resultSet.getInt(columnIndex);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return 0;
    }
    
    public boolean getBoolean(int columnIndex) {
        try {
            return m_resultSet.getBoolean(columnIndex);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return false;
    }    
    
    public short getShort(int columnIndex) {
        try {
            return m_resultSet.getShort(columnIndex);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return 0;
    }    

    public boolean wasNull() {
        try {
            return m_resultSet.wasNull();
        } catch (SQLException e) {
            ApiAlgs.rethrowException(e);
        }
        return false;
    }
    
	public List<String> getColumns() {
	    List<String> res = new ArrayList<String>();
	    if(m_resultSet == null) {
	        return res;
	    }
        ResultSetMetaData md;
        try {
            md = m_resultSet.getMetaData();
            int columnCount = md.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                res.add(md.getColumnName(i));
            }
        } catch (SQLException e) {
            ApiAlgs.rethrowException(e);
        }
		return res;
	}
	
	public boolean first(){
        if(m_resultSet == null) {
            return false;
        }	    
        try {
            return m_resultSet.first();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return false;
	}
    
}
