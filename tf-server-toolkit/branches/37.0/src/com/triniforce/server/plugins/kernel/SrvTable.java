/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.qbuilder.QTable;
import com.triniforce.server.srvapi.SrvApiAlgs2;

public class SrvTable extends QTable {

    protected String m_dbName;

    public SrvTable(Class tableDef, String prefix) {
        super(SrvApiAlgs2.getISODbInfo().getTableDbName(tableDef.getName()), prefix);
    }
    public SrvTable(String tableDefName, String prefix) {
        super(SrvApiAlgs2.getISODbInfo().getTableDbName(tableDefName), prefix);
    }
    public SrvTable(Class tableDef) {
        this( tableDef, "" ); //$NON-NLS-1$
    }    

}
