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
import com.triniforce.server.plugins.kernel.ext.messagebus.PKMessageBus;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
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

    public void initOrFinit(boolean init) {

        String errorText = init?"API initialization errror":"API finalization errror";
        List<IPKExtension> exs = new ArrayList<IPKExtension>(getExtensions().values());
        if(!init){
            Collections.reverse(exs);
        }
        
    	IPKExtension extMb = getExtensions().get(PKMessageBus.class.getName());
   		PKMessageBus mb = (PKMessageBus) (extMb == null? null : extMb.getInstance());
        
        IBasicServer bs = (IBasicServer) getRootExtensionPoint();
        
        
        
        for(IPKExtension ex: exs){
            Object api = ex.getInstance();
            if((init && api instanceof IInitApi) || (!init && api instanceof IFinitApi)){
                try{
                    if(null != bs){
                        //tests
                        bs.enterMode(Mode.Running);
                    }
                    try{
                        if(init){
                            ((IInitApi)api).initApi();    
                        }else{
                            ((IFinitApi)api).finitApi();
                        }
                        if(null != bs){
                            ISrvSmartTranFactory.Helper.commit();
                        }
                    }
                    finally{
                        if(null != bs){
                            //tests
                            bs.leaveMode();
                        }
                    }
                }catch(Exception e){
                    ApiAlgs.logError(this, errorText +": "+ api.getClass().getName(), e);
                }
            }
            
            //subscribe after init
            if(init  && null != mb){
            	mb.subscribeByAnnotation(api);
            }
            
        }
    }

    public void finit(){
        initOrFinit(false);
    }

    public void init() {
        initOrFinit(true);
    }
    
}
