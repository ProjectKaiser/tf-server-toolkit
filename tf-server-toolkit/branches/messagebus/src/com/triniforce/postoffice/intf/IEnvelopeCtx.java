/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

public interface IEnvelopeCtx {
    /**
     * @return An envelope. The envelope may be safely saved and than used in the future.
     */
    Envelope getEnvelope();
}
