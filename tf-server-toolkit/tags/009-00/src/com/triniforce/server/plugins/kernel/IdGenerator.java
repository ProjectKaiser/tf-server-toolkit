/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

import com.triniforce.db.dml.SmartTran;
import com.triniforce.server.plugins.kernel.tables.NextId;
import com.triniforce.server.plugins.kernel.tables.NextId.NextIdBL;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.ISrvPrepSqlGetter;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

/**
 * Class use "nextid" table in database to keep next value to use. "nextid" is
 * updated for every CacheSize-th request. The lowest possible value is defined
 * by SrvApiConsts.MIN_GENERATED_KEY.
 * 
 * Class uses IPooled to get connection since it has it's own transaction
 * management
 * 
 */
public class IdGenerator implements IIdGenerator {
	
    /**
     * Number of cached keys reserved by IdGenerator
     */
    public static final int KEY_CACHE_SIZE = 100;
    
    /**
     * Minimum key value which can be generated for any server object. Lower
     * values are reserved for internal purposes.
     */
    public final static long MIN_GENERATED_KEY = 10000;


    private int m_numCacheSize;

    private long m_currentKey;

    private long m_lastKey;

    private NextId m_genDef;

    /**
     * Construct id generator SrvApi must supply ISODbInfo and Connection
     * interface IdGeneratorTabDef must be in base
     * 
     * @param NumCacheSize
     * @param genDef
     *            Definition for where keys are stored. This object used for
     *            sinchronization.
     */
    public IdGenerator(int NumCacheSize, NextId genDef){
        if (NumCacheSize <= 0)
            throw new IllegalArgumentException(
                    MessageFormat
                            .format(
                                    "Cache size for IdGenerator cannot be ''{0}''", NumCacheSize)); //$NON-NLS-1$

        m_numCacheSize = NumCacheSize;
        m_genDef 	 = genDef;
        m_lastKey 	 = 0;
        m_currentKey = 0;
    }

    /**
     * Load next key from database and reserve keys for cache
     * 
     * @param cacheSize
     * @return next key
     * @throws SQLException
     */
    private long loadCache(int cacheSize){
        synchronized (m_genDef) {
            Connection conn = SrvApiAlgs2.getPooledConnection(ApiStack.getApi());
            SmartTran tr = new SmartTran(conn, (ISrvPrepSqlGetter) ApiStack.getApi().queryIntfImplementor(ISrvPrepSqlGetter.class));

            long first;
            long last;
            try {
            	NextIdBL bl = m_genDef.getBL(tr);
            	Long current = bl.get();
                first = null == current ?  MIN_GENERATED_KEY: current;
                last = first + cacheSize;
            	bl.clear();
            	bl.set(last);
                tr.commit();
            } finally {
                tr.close();
                SrvApiAlgs2.returnPooledConnection(ApiStack.getApi(), conn);
            }
            return first;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.triniforce.server.srvapi.IIdGenerator#getKey()
     */
    public synchronized long getKey() {
        if (m_currentKey == m_lastKey) {
            m_currentKey = loadCache(m_numCacheSize);
            m_lastKey = m_currentKey + m_numCacheSize;
        }
        return m_currentKey++;
    }
    
    public synchronized void setKey(long v){
    	ApiAlgs.assertTrue(v>=MIN_GENERATED_KEY, ""+v);
    	
        synchronized (m_genDef) {
            Connection conn = SrvApiAlgs2.getPooledConnection(ApiStack.getApi());
            SmartTran tr = new SmartTran(conn, (ISrvPrepSqlGetter) ApiStack.getApi().queryIntfImplementor(ISrvPrepSqlGetter.class));

            long first;
            long last;
            try {
            	NextIdBL bl = m_genDef.getBL(tr);
            	Long current = bl.get();
            	if(null != current)
            		ApiAlgs.assertTrue(v > current, ""+v);
                first = v;
                last = first + m_numCacheSize;
            	bl.clear();
            	bl.set(last);
                tr.commit();
            } finally {
                tr.close();
                SrvApiAlgs2.returnPooledConnection(ApiStack.getApi(), conn);
            }
        	m_currentKey = first;
        	m_lastKey = last;
        }
    }
}
