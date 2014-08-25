/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.triniforce.extensions.PluginsLoader;
import com.triniforce.utils.ApiAlgs;

public abstract class PackagesFolder extends ClassesFolder{
    
    void processFolder(File f, Collection<Class> res){
        
        PluginsLoader pl =  new PluginsLoader(f);
        res.addAll(pl.loadClasses());
        
    }
    
    @Override
    synchronized Collection<Class> loadClasses(){
        
        Collection<Class> res = new ArrayList<Class>();
        File parent = getFolder();
        if(null == parent){
            ApiAlgs.getLog(this).warn("Folder value is null");
            return res;
        }
        if(!parent.exists()){
            return res;
        }
        if(!parent.isDirectory()){
            return res;
        }
        for(String n: parent.list()){
            File c = new File(parent, n);
            if(!c.isDirectory()){
                continue;
            }
            processFolder(c, res);
        }
        return res;
        
        
        
    }

}
