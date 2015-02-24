/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.util.Collection;

import com.triniforce.extensions.IPKExtension;
import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiStack;

public class ExternalClasses implements IExternalClasses,  IPKEPAPI{

    public Class getImplementedInterface() {
        return IExternalClasses.class;
    }

    public Collection<Class> listClassesOfType(Class folder, Class superClass){
        IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
        IPKExtension pke = bs.getExtension(PKEPExternalClasses.class, folder);
        ClassesFolder cf = pke.getInstance();
        return cf.listClassesOfType(superClass);
    }

}
