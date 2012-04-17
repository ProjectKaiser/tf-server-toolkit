/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.lang.reflect.Type;

import com.triniforce.soap.TypeDef;

interface IDefLibrary{
    TypeDef get(Type type);
    TypeDef add(Type type);
    //String generateTypeName(Type type);
    
	interface ITypeNameGenerator{
		String get(Type type, String template, boolean bThrowIfExists);
	}
}
