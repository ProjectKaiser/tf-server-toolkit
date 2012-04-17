/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.utils;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileAlgs {
    
    public static void zipFolderToEntry(ZipOutputStream zout, ZipEntry entry, File srcDir){
		TFUtils.zipFolderToEntry(zout, entry, srcDir);
	}
    
    public static void delTree(File dir, boolean deleteDirItSelf) {
		TFUtils.delTree(dir, deleteDirItSelf);
	}

    public static void printlnToFile(File f, String s) {
		TFUtils.printlnToFile(f, s);
	}
}
