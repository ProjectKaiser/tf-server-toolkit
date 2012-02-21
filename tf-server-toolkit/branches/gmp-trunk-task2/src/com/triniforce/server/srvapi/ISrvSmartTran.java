/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.srvapi;

import com.triniforce.db.dml.ISmartTran;

public interface ISrvSmartTran extends ISmartTran {

    void registerAffectedQueue(IDbQueue fq);

}
