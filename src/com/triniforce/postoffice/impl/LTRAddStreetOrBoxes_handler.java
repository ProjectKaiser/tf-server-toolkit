/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.UUID;

import com.triniforce.postoffice.intf.LTRAddStreetOrBoxes;
import com.triniforce.postoffice.intf.NamedPOBoxes;

public class LTRAddStreetOrBoxes_handler{
    static Object process(PostMaster pm, EnvelopeCtx ctx, LTRAddStreetOrBoxes data, Outboxes outs){

        Street ws = pm.m_rootStreet;
        
        Street targetBoxStreet = ws.queryPath(data.getStreetPath());
        if(null == targetBoxStreet){
            return null;
        }
        
        //Create new street if needed
        {
            if(null != data.getNewStreetName()){
                targetBoxStreet = new Street(targetBoxStreet);
            }
        }
        
        //Create box wrappers list
        NamedPOBoxWrappers newBoxesw = new NamedPOBoxWrappers();        
        {
            NamedPOBoxes requestedBoxes = data.getBoxes(); 
            if( null != requestedBoxes){
                for(String boxName :requestedBoxes.keySet()){
                    POBoxWrapper boxw = new POBoxWrapper(targetBoxStreet, requestedBoxes.get(boxName), UUID.randomUUID());
                    newBoxesw.put(boxName, boxw);
                }
            }
        }
        

        //Initialize boxes
        {
            for(POBoxWrapper boxw: newBoxesw.values()){
                Outbox out = new Outbox();
                boxw.getBox().priorProcess(out);
                outs.put(boxw, out);
            }
        }
        
        //Connect boxes to targetStreet and root map
        {
        
            targetBoxStreet.getBoxes().putAll(newBoxesw);
            
            for(POBoxWrapper boxw: newBoxesw.values()){
                pm.m_boxWrappers.put(boxw.getUuid(), boxw);
            }

            //connect street to parent street
            if(null != data.getNewStreetName()){
                targetBoxStreet.getParent().getStreets().put(data.getNewStreetName(), targetBoxStreet);
            }
            
        }
        
        return null;
    }

}
