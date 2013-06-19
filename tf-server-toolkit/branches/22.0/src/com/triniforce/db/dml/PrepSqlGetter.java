/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.dml;

import java.util.HashMap;

import com.triniforce.utils.ApiAlgs;

public class PrepSqlGetter implements IPrepSqlGetter {

    protected HashMap<Class, String> m_map = new HashMap<Class, String>();

   
    public synchronized String getSql(Class prepSql) {
        String res = m_map.get(prepSql);
        if (null != res)
            return res;
        try {
            PrepSql b = (PrepSql) prepSql.newInstance();
            res = b.buildSql().toString();
            m_map.put(prepSql, res);
            return res;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }
}
