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
    static Object process(PostMaster pm, EnvelopeCtx ctx, Object data){

        LTRAddStreetOrBoxes ltr = (LTRAddStreetOrBoxes) data;
        
        Street ws = pm.m_rootStreet;
        
        Street targetBoxStreet = ws.queryPath(ltr.getStreetPath());
        if(null == targetBoxStreet){
            return null;
        }
        
        //Create new street if needed
        {
            if(null != ltr.getNewStreetName()){
                targetBoxStreet = new Street(targetBoxStreet);
            }
        }
        
        //Create box wrappers list
        NamedPOBoxWrappers newBoxesw = new NamedPOBoxWrappers();        
        {
            NamedPOBoxes requestedBoxes = ltr.getBoxes(); 
            if( null != requestedBoxes){
                for(String boxName :requestedBoxes.keySet()){
                    POBoxWrapper boxw = new POBoxWrapper(targetBoxStreet, requestedBoxes.get(boxName), UUID.randomUUID());
                    newBoxesw.put(boxName, boxw);
                }
            }
        }
        

        //Initialize boxes
        {
            
        }
        
        //Connect boxes to targetStreet and root map
        {
        
            targetBoxStreet.getBoxes().putAll(newBoxesw);
            
            for(POBoxWrapper boxw: newBoxesw.values()){
                pm.m_boxWrappers.put(boxw.getUuid(), boxw);
            }

            //connect street to parent street
            if(null != ltr.getNewStreetName()){
                targetBoxStreet.getParent().getStreets().put(ltr.getNewStreetName(), targetBoxStreet);
            }
            
        }
        
        //Send initialization messages
        {
            
        }
        
        return null;
    }

}
