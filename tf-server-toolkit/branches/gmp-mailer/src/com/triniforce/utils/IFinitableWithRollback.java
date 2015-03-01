/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

/**
 * Rollback is called when overall transaction is being rolled back
 */
public interface IFinitableWithRollback extends IFinitable{
    void rollback();
}
