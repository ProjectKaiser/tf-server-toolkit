/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IFinitable;
import com.triniforce.utils.Utils;

public class ResultSetWalker<T> implements Iterator<T>, IFinitable{
	
	public interface IObjectFactory<T> {
		T createObject(ResultSet rs);
		void addRow(T obj, ResultSet rs);
	}

	//private boolean m_bHasNext;
	private IObjectFactory<T> m_objFact;
	private ResultSet m_rs;
	private int m_key;
	private Object m_objKey;
	private T m_nextObj;
	
	public ResultSetWalker(IObjectFactory<T> objFact, ResultSet rs, String keyName) throws SQLException {
		this(objFact,rs, rs.findColumn(keyName));
	}

	public ResultSetWalker(IObjectFactory<T> objFact, ResultSet rs, int keyIdx) throws SQLException {
		m_objFact = objFact;
		m_rs = rs;
		m_key = keyIdx;
		if(m_rs.next()){
			m_objKey = m_rs.getObject(m_key);
			getNext();
		}
		else
			m_nextObj = null;
	}

	public boolean hasNext() {
		return null != m_nextObj;
	}

	public T next() {
		try {
			T res = m_nextObj;
			if(null == res)
				return null;
			Object nextObjKey = null;
			boolean bHasNext;
			do{
				m_objFact.addRow(res, m_rs);
				bHasNext = m_rs.next();
				if(bHasNext)
					nextObjKey = m_rs.getObject(m_key);
				else
					break;
			} while(Utils.equals(m_objKey, nextObjKey));
			if(bHasNext){
				m_objKey = nextObjKey;
				getNext();
			}
			else
				m_nextObj = null;
			return res;
		} catch (SQLException e) {
			ApiAlgs.rethrowException(e);
		}
		return null;
	}
	
	void getNext(){
		try {
			do{
				m_nextObj = m_objFact.createObject(m_rs);
				if(null != m_nextObj){
					break;
				}
				boolean bHasNext = m_rs.next();
				if(bHasNext){
					m_objKey = m_rs.getObject(m_key);
				}
				else{
					break;
				}
			}while(true);
		} catch (SQLException e) {
			ApiAlgs.rethrowException(e);
		}
	}

	public void remove() {}

	public void finit() {
		m_nextObj = null;
		try {
			m_rs.close();
		} catch (SQLException e) {
			ApiAlgs.rethrowException(e);
		}
	}
	
}
