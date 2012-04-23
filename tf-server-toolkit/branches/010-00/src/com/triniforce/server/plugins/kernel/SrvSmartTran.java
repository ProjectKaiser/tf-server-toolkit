/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.triniforce.db.dml.SmartTran;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.ISrvPrepSqlGetter;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.ISrvSmartTranFactory.ITranExtender;
import com.triniforce.utils.ApiAlgs;

public class SrvSmartTran extends SmartTran implements ISrvSmartTran {

    private ArrayList<IDbQueue> m_updatedQueues;
    
    //TODO gmp:будет проблема с инициализацией/финализацией
    public SrvSmartTran(Connection connection, ISrvPrepSqlGetter sqlGetter) {
        //Here we use queryIntf since it can be not installed
        super(connection, sqlGetter);
        try {
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} catch (SQLException e) {
			ApiAlgs.rethrowException(e);
		}
        m_updatedQueues = new ArrayList<IDbQueue>();
        
        ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
        
    	for (ITranExtender trnExtender : trf.getOuterExtenders()) {
            trnExtender.push();
        }
        for (ITranExtender trnExtender : trf.getInnerExtenders()) {
            trnExtender.push();
        }
    }
    
    @Override
    public void close(boolean bCommit) {
        //TODO gmp:test exception during commit. плохо с warn.
        if (isClosed())
            return;
        
        ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
        
        RuntimeException eFirstProblem = null;
        
        {
	    	List<ITranExtender> extenders = trf.getInnerExtenders(); 
	    	ListIterator<ITranExtender> iExtenders = extenders.listIterator(extenders.size());
	    	while(iExtenders.hasPrevious()){
	    		try{
	    			ITranExtender trnExtender = iExtenders.previous();
	    			trnExtender.pop(bCommit);
	    		} catch(RuntimeException t){
                    if( null == eFirstProblem ){
                        eFirstProblem = t;
                    }
                    bCommit = false;
	    			ApiAlgs.getLog(this).warn("", t);
	    		}
			}
        }
        
    	super.close(bCommit);
    	
    	{
	    	List<ITranExtender> extenders = trf.getOuterExtenders(); 
	    	ListIterator<ITranExtender> iExtenders = extenders.listIterator(extenders.size());
	    	while(iExtenders.hasPrevious()){
	    		try{
	    			ITranExtender trnExtender = iExtenders.previous();
	    			trnExtender.pop(bCommit);
				} catch(RuntimeException t){
					ApiAlgs.getLog(this).error("", t);
				}
			}
    	}
    	
        for (IDbQueue queue : m_updatedQueues) {
            synchronized (queue) {
                queue.notify();
            }
        }
        
        if( null != eFirstProblem){
            throw eFirstProblem;
        }
        
    }

	public void registerAffectedQueue(IDbQueue fq) {
		m_updatedQueues.add(fq);
	}
}
