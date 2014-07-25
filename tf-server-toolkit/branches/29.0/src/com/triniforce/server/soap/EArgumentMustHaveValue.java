/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

@SuppressWarnings("serial")
public class EArgumentMustHaveValue extends RuntimeException{
    public EArgumentMustHaveValue(String argName) {
        super("Argument \"" + argName + "\" must have value");
    }
}