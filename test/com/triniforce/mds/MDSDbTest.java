/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import com.triniforce.db.ddl.ActualStateBL.TIndexNames;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;

public class MDSDbTest extends BasicServerTestCase{

    public void test() throws Exception {
        
        m_server.enterMode(Mode.Running);
        try{
            ISrvSmartTran  st = ApiStack.getInterface(ISrvSmartTran.class);
            ResSet rs = st.select(TIndexNames.class, new IName[]{TIndexNames.appName}, new IName[]{}, new IName[]{});
            trace(rs);
        }finally{
            m_server.leaveMode();
        }
        
    }

}
