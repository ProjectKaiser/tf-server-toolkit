/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.recurring;

import java.text.MessageFormat;

import com.triniforce.db.dml.ISmartTran;
import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ICheckInterrupted;

public class PKEPRecurringTasks extends PKExtensionPoint{
    
    public static final int DEFAULT_PAST_THRESHOLD = 1000 * 3600 * 24;
    public static final int ALWAYS_READ_INTERVAL = 1000 * 3600;//per hour
    
    /**
     * 0 always read
     */
    private long m_nextTime;
    private int m_exceptionCount;
    
    public PKEPRecurringTasks() {
        setSingleExtensionInstances(true);
        setExtensionClass(IPKEPRecurringTask.class);
    }

    
    public TRecurringTasks.Data peekNextData(){
        TRecurringTasks.BL bl = ISmartTran.Helper.instantiateBL(TRecurringTasks.BL.class);
        return bl.selectFirst();
    }

    public synchronized void createOrUpdateTask(long id, Class extClass, long astart){
        createOrUpdateTask(id, extClass, astart, null, 0, null);
    }
    
    public synchronized void createOrUpdateTask(long id, Class extClass, long astart, RTPeriod period, long current, String timeZone){
        INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
        
        long extId;
        {
            //check that extension exists
            getExtension(extClass);
            extId = dbId.createId(extClass.getName());            
        }
        
        long start;
        long pastThreshold = DEFAULT_PAST_THRESHOLD;
        {
            if(null == period){
                start = astart;
            }else{
                start = period.calcNextOccurence(astart, current, timeZone);
                pastThreshold = period.getPastThreshold();
            }
        }
        TRecurringTasks.BL bl = ISmartTran.Helper.instantiateBL(TRecurringTasks.BL.class);
        bl.delete(id);
        bl.insert(id, extId, start, pastThreshold);
        m_nextTime = 0;
    }
    
    public synchronized void deleteTask(long id){
        TRecurringTasks.BL bl = ISmartTran.Helper.instantiateBL(TRecurringTasks.BL.class);    
        bl.delete(id);
    }
    
    public synchronized void deleteAllTasks(){
        TRecurringTasks.BL bl = ISmartTran.Helper.instantiateBL(TRecurringTasks.BL.class);    
        bl.deleteAll();
        ISrvSmartTranFactory.Helper.commitAndStartTran();
    }
    
    public int processTasksInTransactions(long currentTime){
        if(currentTime < m_nextTime){
            return 0;
        }
        
        INamedDbId dbId= ApiStack.getInterface(INamedDbId.class);
        
        int res = 0;
        while(true){
            TRecurringTasks.Data data = null;
            ICheckInterrupted.Helper.checkInterrupted();
            synchronized(this) {
                try{
                    TRecurringTasks.BL bl = ISmartTran.Helper.instantiateBL(TRecurringTasks.BL.class);
                    data = bl.selectFirst();
                    if(null == data){
                        m_nextTime = currentTime + ALWAYS_READ_INTERVAL;
                        break;
                    }
                    if(data.start > currentTime){
                        m_nextTime = data.start;
                        break;
                    }
                    bl.delete(data.id);                
                    String extId = dbId.getName(data.extension_id);
                    IPKEPRecurringTask rt = getExtension(extId).getInstance();
                    try{
                        rt.processTask(data.id, data.start, currentTime, currentTime - data.start > data.past_threshold);
                    }catch(Throwable t){
                        m_exceptionCount++;
                        String s=MessageFormat.format("Error processing {0}.{1}, id is skipped", extId, data.id);
                        ApiAlgs.getLog(this).error(s, t);
                        //rollback
                        ISrvSmartTranFactory.Helper.pop();
                        ISrvSmartTranFactory.Helper.push();
                        //delete record                    
                        {
                            TRecurringTasks.BL bl2 = ISmartTran.Helper.instantiateBL(TRecurringTasks.BL.class);
                            bl2.delete(data.id);
                        }
                    }
                    ISrvSmartTranFactory.Helper.commit();
                    res++;                
                }finally{
                    ISrvSmartTranFactory.Helper.pop();
                    ISrvSmartTranFactory.Helper.push();
                }
            }//synchronized(this);
        }//while
        return res;
    }

    public void setNextTime(long nextTime) {
        m_nextTime = nextTime;
    }

    public long getNextTime() {
        return m_nextTime;
    }


    public int getExceptionCount() {
        return m_exceptionCount;
    }
    
}
