/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 

package com.triniforce.server.plugins.kernel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.triniforce.db.dml.PrepStmt;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.dml.StmtContainer;
import com.triniforce.server.plugins.kernel.tables.TDbQueues;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class DbQueue implements IDbQueue {
	private final long m_queueId;
	
	public DbQueue(long queueId) {
		m_queueId = queueId;
	}

	synchronized public Object get(long timeoutMilliseconds) {
		DbRecord dbRec = peekInternal(timeoutMilliseconds);
		Object res = null;
		if(null != dbRec){
			res = dbRec.obj;
			TDbQueues.PQDelete.exec(m_queueId, dbRec.recId);				
		}
		return res;
	}

	synchronized public Object peek(long timeoutMilliseconds) {
		DbRecord dbRec = peekInternal(timeoutMilliseconds);
        Object res;
        if(null == dbRec)
            res = null;
        else{
            res = dbRec.obj;
        }
		return res;
	}
	
	static class DbRecord{
		Object obj;	
		long recId;
	}
	
	public DbRecord peekInternal(long timeoutMilliseconds) {
		
		DbRecord res = readDbRecord();
        
        ISrvSmartTranFactory trnFact = ApiStack.getApi().getIntfImplementor(ISrvSmartTranFactory.class);
		if(null == res && timeoutMilliseconds > 0){
            trnFact.pop();
            try{
				wait(timeoutMilliseconds);
			} catch (InterruptedException e) {
				ApiAlgs.rethrowException(e);
            } finally{
                trnFact.push();
            }
			res = readDbRecord();
		}

		return res;
	}

	public void put(Serializable data) {
		InputStream is = writeObjectInStream(data);
		try {
			long recId = SrvApiAlgs2.generateId();
			TDbQueues.PQInsert.exec(recId, m_queueId, is, is.available());
			is.close();
			ISrvSmartTran srvTran = SrvApiAlgs2.getIServerTran();
			srvTran.registerAffectedQueue(this);
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		}		
	}
	
	private Object readObjectFromStream(InputStream is) {
		Object res = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			res = ois.readObject();
			ois.close();
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		} catch (ClassNotFoundException e) {
			ApiAlgs.rethrowException(e);
		}
		return res;
	}

	private InputStream writeObjectInStream(Serializable data) {
		ByteArrayInputStream res = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(data);
			oos.close();
			res = new ByteArrayInputStream(baos.toByteArray());
			baos.close();
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		}
		return res;
	}

	private DbRecord readDbRecord() {
		DbRecord res = null;
		StmtContainer sc = SrvApiAlgs2.getStmtContainer();
		try{
			ResSet rs = TDbQueues.PQGetHead.exec(sc, m_queueId);
			if(rs.next()){
				res = new DbRecord();
				res.recId = rs.getLong(1);
				InputStream is = rs.getResultSet().getBlob(2).getBinaryStream();
				try{
					res.obj = readObjectFromStream(is);
				}finally{
					is.close();
				}
			}
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		} finally{
			sc.close();
		}		
		return res;
	}

    public void removeFromHead() {
        StmtContainer sc = SrvApiAlgs2.getStmtContainer();
        try{
            String dbName = SrvApiAlgs2.getISODbInfo().getTableDbName(TDbQueues.class.getName());
            PrepStmt ps = sc.prepareStatement("select min(id) from " + dbName); //$NON-NLS-1$
            ResSet rs = ps.executeQuery();
            if(rs.next()){
                long id = rs.getLong(1);
                if(!rs.wasNull()){
                    TDbQueues.PQDelete.exec(sc, m_queueId, id);       
                }
            }
        } finally{
            sc.close();
        }       
    }

    public void clean() {
        TDbQueues.PQDeleteAll.exec(m_queueId);
    }

}
