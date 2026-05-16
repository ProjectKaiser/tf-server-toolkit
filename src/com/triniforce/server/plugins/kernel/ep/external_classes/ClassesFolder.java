/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.triniforce.extensions.PluginsLoader;

/**
 * Location folder where jars should be loaded from
 */
public abstract class ClassesFolder implements Closeable{
    
    public ClassesFolder() {
    }
    
    Collection<Class> m_classes = null;
    PluginsLoader m_loader = null;

    public abstract File getFolder();
    
    synchronized Collection<Class> loadClasses(){
        m_loader = new PluginsLoader(getFolder());
        return m_loader.loadClasses();
    }
    
    Collection<Class> listClassesOfType(Class superClass){
        synchronized (this) {
            if (null == m_classes) {
                m_classes = loadClasses();
            }
        }
        
        Collection<Class> res =new ArrayList<Class>();
        
        for(Class cls: m_classes){
            if(superClass.isAssignableFrom(cls)){
                res.add(cls);
            }
        }
        return res;
    }


    @Override
    public synchronized void close() throws IOException {
        if (m_loader != null) {
            m_loader.close();
            m_loader = null;
        }
        m_classes = null;
    }

}
