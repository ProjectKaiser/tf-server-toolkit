/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

public interface IQContext {
    IQTable getTable(String prefix) throws Err.EPrefixNotFound;
}
