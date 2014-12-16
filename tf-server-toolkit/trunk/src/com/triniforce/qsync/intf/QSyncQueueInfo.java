/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.qsync.intf;


public class QSyncQueueInfo {

    public QSyncTaskResult result;

    /**
     * Last synced, milliseconds
     */
    public long lastSynced;
    public long lastError;
    public long lastAttempt;


}
