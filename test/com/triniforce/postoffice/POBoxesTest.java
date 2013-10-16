/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import com.triniforce.db.test.TFTestCase;

public class POBoxesTest extends TFTestCase{
    
    static class MyPOBox implements IPOBox{
        public void process(IEnvelope env, Object data, IOutbox out) {
        }

        public void processShutdown(IEnvelope env, Object data, IOutbox out) {
        }
    }
    
    @Override
    public void test() throws Exception{
        POBoxes boxes = new POBoxes();
        IPOBox box1 = new MyPOBox();
        IPOBox box2 = new MyPOBox();
        IPOBox box3 = new MyPOBox();
        boxes.put("box1", box1);
        assertSame(box1, boxes.get("box1"));
    }

}
