/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.postoffice.intf;

import com.triniforce.db.test.TFTestCase;

public class NamedPOBoxesTest extends TFTestCase {

    static class MyPOBox implements IPOBox {

        public void process(IEnvelopeCtx ctx, Object data, IOutbox out) {
            
        }

        public void beforeDisconnect(int intervalMs) {
            
        }

        public void onDisconnect() {
            
        }

        public void priorProcess(IOutbox out) {
        }
    }

    @Override
    public void test() throws Exception {
        NamedPOBoxes boxes = new NamedPOBoxes();
        IPOBox box1 = new MyPOBox();
        IPOBox box2 = new MyPOBox();
        boxes.put("box1", box1);
        boxes.putByClass(box2);
        assertSame(box1, boxes.get("box1"));
        assertSame(box2, boxes.get(box2.getClass().getName()));
    }

}
