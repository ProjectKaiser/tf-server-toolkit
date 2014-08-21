/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.utils.TFUtils;

public class PluginsLoaderTest extends TFTestCase {
    
    public static void copyTestPlugins(File dstFolder) throws Exception{
        String resources[] = new String[]{"pk-webcontent-jquery.jar", "pk-macros-google.jar"};
        
        for(String resource: resources){
            InputStream is = PluginsLoaderTest.class.getResourceAsStream(resource);
            OutputStream os = new FileOutputStream(new File(dstFolder, resource));
            TFUtils.copyStream(is, os);
            is.close();
            os.close();
        }
    }
    
    @Override
    public void test() throws Exception {
        File tmpFolder = getTmpFolder(this);
        copyTestPlugins(tmpFolder);
        
        PluginsLoader pl = new PluginsLoader(tmpFolder);
        //test jar files
        {
            List<File> jars = pl.getJarFiles();
            assertEquals(4, jars.size());
        }
        // test readPluginClass
        {
            for(File jar: pl.getJarFiles()){
                assertFalse(TFUtils.isEmptyString(pl.readPluginClass(jar)));
            }
        }
        List<IPlugin> plugins = pl.loadPlugins();
        assertEquals(2, plugins.size());
    }
    
}
