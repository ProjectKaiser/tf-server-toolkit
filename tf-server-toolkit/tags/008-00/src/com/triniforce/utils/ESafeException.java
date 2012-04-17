/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.utils;

public class ESafeException extends RuntimeException {
    private static final long serialVersionUID = 1006245740047915689L;
    public ESafeException(String msg) {
        super(msg);
    }
    public ESafeException(){
    }
}
