/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.br;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.plugins.kernel.ep.br.IBackupStorage;
import com.triniforce.server.plugins.kernel.ep.br.IRestoreStorage;
import com.triniforce.server.plugins.kernel.ep.br.PKEPBackupRestoreEntry;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IPropSerializabe;
import com.triniforce.utils.VersionComparator;

public class BackupRestorePluginVersions extends PKEPBackupRestoreEntry {
    public static class PluginVersions implements IPropSerializabe{
        private Map<String, String> m_pluginVersions = new HashMap<String, String>();
        public PluginVersions(){
            
        }
        public PluginVersions(IPKRootExtensionPoint rep){
            IPKExtensionPoint ep = rep.getEpPlugins();
            for(String key: ep.getExtensions().keySet()){
                Object plugin = ep.getExtension(key).getInstance();
                if( plugin instanceof PKPlugin){
                    getPluginVersions().put(key, ((PKPlugin)plugin).getVersion());                    
                }
            }
        }
        public void setPluginVersions(Map<String, String> pluginVersions) {
            m_pluginVersions = pluginVersions;
        }
        public Map<String, String> getPluginVersions() {
            return m_pluginVersions;
        }
    }

    @Override
    public void backup(IBackupStorage stg) {
        IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
        stg.writeObject(KEY_DATA, new PluginVersions(bs));
    }

    @Override
    public String initRestore(IRestoreStorage stg) {
        PluginVersions archivePV = (PluginVersions) stg.readObject(KEY_DATA);
        IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
        PluginVersions currentPV = new PluginVersions(bs);
        if( VersionComparator.compareVersions(currentPV.getPluginVersions(), archivePV.getPluginVersions()) < 0){
            return "Archive contains newer versions of plugins";
        }
        return null;
    }

    @Override
    public void restore(IRestoreStorage stg) {
    }
    
}
