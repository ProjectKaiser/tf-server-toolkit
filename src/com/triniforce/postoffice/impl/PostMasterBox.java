/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.triniforce.postoffice.intf.IEnvelopeCtx;
import com.triniforce.postoffice.intf.IOutbox;
import com.triniforce.postoffice.intf.IPOBox;
import com.triniforce.postoffice.intf.LTRAddStreetOrBoxes;
import com.triniforce.postoffice.intf.LTRListBoxes;
import com.triniforce.postoffice.intf.LTRListStreets;
import com.triniforce.postoffice.intf.NamedPOBoxes;

/**
 * Is NOT connected to root street
 */
public class PostMasterBox implements IPOBox{
    
    private final PostMaster m_pm;

    public PostMasterBox(PostMaster pm){
        m_pm = pm;
    }
    
    void process_LTRListStreets(IEnvelopeCtx ctx, LTRListStreets data, IOutbox out){
        Street ws = m_pm.m_rootStreet.queryPath(data.getStreetPath());
        out.reply(ctx.getEnvelope(), new ArrayList<String>(ws.getStreets().keySet()), null);
    }
    
    void process_LTRListBoxes(IEnvelopeCtx ctx, LTRListBoxes data, IOutbox out){
        
        Street ws = m_pm.m_rootStreet.queryPath(data.getStreetPath());
        if( null == ws){
            return;
        }
        NamedPOBoxWrappers boxws = ws.getBoxes();
        
        Map<String, UUID> res = new HashMap<String, UUID>();
        
        for(String name: boxws.keySet()){
            POBoxWrapper boxw = boxws.get(name);
            if(null == boxw){
                continue;
            }
            res.put(name, boxw.getUuid());
        }
        out.reply(ctx.getEnvelope(), res, null);
    }
    
    void process_LTRAddStreetOrBoxes(IEnvelopeCtx ctx, LTRAddStreetOrBoxes data, IOutbox out){

        Street ws = m_pm.m_rootStreet;
        
        Street targetBoxStreet = ws.queryPath(data.getStreetPath());
        if(null == targetBoxStreet){
            return;
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
                ((Outbox)out).setDefSender(boxw.getUuid());
                boxw.getBox().priorProcess(out);
            }
        }
        
        //Connect boxes to targetStreet and root map
        {
        
            targetBoxStreet.getBoxes().putAll(newBoxesw);
            
            for(POBoxWrapper boxw: newBoxesw.values()){
                m_pm.m_boxWrappers.put(boxw.getUuid(), boxw);
            }

            //connect street to parent street
            if(null != data.getNewStreetName()){
                targetBoxStreet.getParent().getStreets().put(data.getNewStreetName(), targetBoxStreet);
            }
            
        }
    }

    public void process(IEnvelopeCtx ctx, Object data, IOutbox out) {
        if( data instanceof LTRListStreets){
            process_LTRListStreets(ctx, (LTRListStreets) data, out);
        }
        if( data instanceof LTRListBoxes){
            process_LTRListBoxes(ctx, (LTRListBoxes) data, out);
        }
        if( data instanceof LTRAddStreetOrBoxes){
            process_LTRAddStreetOrBoxes(ctx, (LTRAddStreetOrBoxes) data, out);
        }
        
    }

    public void priorProcess(IOutbox out) {
        // TODO Auto-generated method stub
        
    }

    public void onDisconnect() {
        // TODO Auto-generated method stub
        
    }

}
