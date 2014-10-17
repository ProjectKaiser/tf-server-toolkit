/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.util.Collection;

public interface IExternalClasses {
    Collection<Class> listClassesOfType(Class folder, Class superClass);    
}
