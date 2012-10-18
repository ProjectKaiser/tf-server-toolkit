/*
 *
 * (c) Triniforce
 *
 */
package com.triniforce.server.srvapi;

import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ICheckInterrupted;

public interface IDbQueueFactory {
    /**
     * 
     * Returns interface to work with queue with given parameters
     * 
     * @param parentId
     * @return
     */
    IDbQueue getDbQueue(long parentId);

    public static class Helper {
        public static IDbQueue getQueue(long parentId) {
            IDbQueueFactory f = ApiStack.getInterface(IDbQueueFactory.class);
            return f.getDbQueue(parentId);
        }
        
        public static void waitForEmptyQueue(long id){
            IDbQueue q= getQueue(id);
            while(null != q.get(0))ICheckInterrupted.Helper.sleep(200);
        }

        public static IDbQueue cleanQueue(long id) {
            ISrvSmartTranFactory.Helper.push();
            try {
                IDbQueue q = IDbQueueFactory.Helper.getQueue(id);
                q.clean();
                ISrvSmartTranFactory.Helper.commit();
                return q;
            } finally {
                ISrvSmartTranFactory.Helper.pop();
            }
        }
    }
}
