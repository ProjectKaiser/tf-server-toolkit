/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.utils.pipe;

public interface IPipeElementFeedback {
    void setStopped(boolean value);

    /**
     * if true, data is not passed to further elements
     */
    boolean isStopped();
}
