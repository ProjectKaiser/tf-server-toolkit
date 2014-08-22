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

public abstract class PackagesFolder extends ClassesFolder{
    
    void processFolder(File f, Collection<Class> res){
        
        PluginsLoader pl =  new PluginsLoader(f);
        res.addAll(pl.loadClasses());
        
    }
    
    @Override
    synchronized Collection<Class> loadClasses(){
        
        Collection<Class> res = new ArrayList<Class>();
        File parent = getFolder();
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
