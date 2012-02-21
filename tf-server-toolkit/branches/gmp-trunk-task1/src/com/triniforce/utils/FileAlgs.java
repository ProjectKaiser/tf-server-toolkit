/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.utils;

import java.io.File;

public class FileAlgs {
    public static void delTree(File dir, boolean deleteDirItSelf) {
        if (!dir.exists())
            return;
        for (File subFile : dir.listFiles()) {
            //String name = subFile.getName();
            if (subFile.isDirectory()) {
                delTree(subFile, true);
            }
            subFile.delete();
        }
        if(deleteDirItSelf){
            dir.delete();
        }
    }
}
