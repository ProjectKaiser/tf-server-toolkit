/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

public class EColumnNotFound  extends RuntimeException{
    private static final long serialVersionUID = 3868615444383500153L;

    public EColumnNotFound(String s) {
        super("Column name: " + s);
    }
}
