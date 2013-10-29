/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.postoffice;

import com.triniforce.db.test.TFTestCase;

public class POBoxesTest extends TFTestCase {

    static class MyPOBox implements IPOBox {
        public void process(Envelope env, Object data, IOutbox out) {
        }

        public void processShutdown(Envelope env, Object data, IOutbox out) {
        }

        public void shutdown(int pauseMs) {
            
        }

        public void connect() {
            
        }

        public void disconnect() {
        }

        public void process(IEnvelopeCtx ctx, Object data, IOutbox out) {
            // TODO Auto-generated method stub
            
        }

        public void onShutdown(int intervalMs) {
            // TODO Auto-generated method stub
            
        }

        public void onStop() {
            // TODO Auto-generated method stub
            
        }
    }

    @Override
    public void test() throws Exception {
        POBoxes boxes = new POBoxes();
        IPOBox box1 = new MyPOBox();
        IPOBox box2 = new MyPOBox();
        boxes.put("box1", box1);
        boxes.put(box2);
        assertSame(box1, boxes.get("box1"));
        assertSame(box2, boxes.get(box2.getClass().getName()));
    }

}
