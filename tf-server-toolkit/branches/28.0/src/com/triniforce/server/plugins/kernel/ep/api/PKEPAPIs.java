/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor;
import com.triniforce.utils.ApiAlgs;

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

    public void finit() {
        List<IPKExtension> exs = new ArrayList<IPKExtension>(getExtensions().values());
        Collections.reverse(exs);
        
        for(IPKExtension ex: exs){
            Object api = ex.getInstance();
            if(api instanceof IFinitApi){
                IFinitApi f = (IFinitApi) api;
                try{
                    f.finitApi();
                }catch(Exception e){
                    ApiAlgs.getLog(this).error("Finalization errror", e);                    
                }
            }
        }
    }

}
