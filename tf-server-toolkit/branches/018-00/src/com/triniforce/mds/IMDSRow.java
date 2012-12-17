/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.mds;

import java.util.List;

public interface IMDSRow extends Iterable<Object> {
    Object get(int idx) throws IndexOutOfBoundsException;
    void set(int idx, Object value) throws IndexOutOfBoundsException;
    void add(Object value);
    void appendArray(Object values[]);
    void appendList(List values);
    void appendArray(Object values[], int fromIndex, int cnt);
    void appendList(List values, int fromIndex, int cnt);
    int size();
}
