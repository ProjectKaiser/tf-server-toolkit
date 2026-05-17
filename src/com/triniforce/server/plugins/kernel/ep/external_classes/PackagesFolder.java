/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.triniforce.extensions.PluginsLoader;
import com.triniforce.utils.ApiAlgs;

public abstract class PackagesFolder extends ClassesFolder{
	
	private ClassLoader m_parentClassLoader = null;
    private List<PluginsLoader> m_loaders = new ArrayList<PluginsLoader>();
	   
    public ClassLoader getParentClassLoader() {
		return m_parentClassLoader;
	}

	public void setParentClassLoader(ClassLoader parentClassLoader) {
		this.m_parentClassLoader = parentClassLoader;
	}

	void processFolder(File f, Collection<Class> res){        
        PluginsLoader pl;
        if (m_parentClassLoader == null)
        	pl = new PluginsLoader(f);
        else
        	pl = new PluginsLoader(f, m_parentClassLoader);
        m_loaders.add(pl);
        res.addAll(pl.loadClasses());        
    }
    
    @Override
    synchronized Collection<Class> loadClasses(){

        for (PluginsLoader pl : m_loaders) {
            try {
                pl.close();
            } catch (IOException e) {
                ApiAlgs.getLog(this).error("Error closing previous PluginsLoader", e);
            }
        }
        m_loaders.clear();

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


    @Override
    public synchronized void close() throws IOException {
        for (PluginsLoader pl : m_loaders) {
            try {
                pl.close();
            } catch (IOException e) {
                ApiAlgs.getLog(this).error("Error closing PluginsLoader", e);
            }
        }
        m_loaders.clear();
        super.close();
    }

}
