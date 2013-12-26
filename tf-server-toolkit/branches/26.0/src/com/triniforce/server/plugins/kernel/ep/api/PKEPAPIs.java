/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.api;

import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor;

public class PKEPAPIs  extends PKExtensionPoint{
    public PKEPAPIs(){
        setExtensionClass(IPKEPAPI.class);
        setSingleExtensionInstances(true);
    }
    
    public void scheduleTasks(PeriodicalTasksExecutor pte){
      for(IPKExtension ex: getExtensions().values()){
          Object api = ex.getInstance();
          if(api instanceof PKEPAPIPeriodicalTask){
              PKEPAPIPeriodicalTask task = (PKEPAPIPeriodicalTask) api;
              pte.scheduleWithFixedDelay(task, task.initialDelay,
                      task.delay, task.unit);
          }
      }
    }

}
