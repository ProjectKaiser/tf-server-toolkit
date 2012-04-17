/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package gmp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.TFUtils;

public class ZipTest extends TFTestCase {

    public void zipFile(ZipOutputStream zout, ZipEntry entry, File srcDir) {
        try {
            for (File f : srcDir.listFiles()) {
                trace(f.getAbsolutePath());
                String zipEntryName = f.isFile() ? entry.getName() + f.getName() : entry.getName()
                                + f.getName() + "/";
                ZipEntry fileEntry = new ZipEntry(zipEntryName);
                fileEntry.setTime(f.lastModified());
                zout.putNextEntry(fileEntry);
                if (f.isDirectory()) {
                    zipFile(zout, fileEntry, f);
                } else {
                    FileInputStream fin = new FileInputStream(f);
                    byte[] buffer = new byte[4096];
                    long sz = 0;
                    for (int n; (n = fin.read(buffer)) > 0;){
                        zout.write(buffer, 0, n);
                        sz += n;
                    }
                    fin.close();
                    //fileEntry.setSize(sz);
                }
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }
    
    public void zip(File srcDir, OutputStream out){
        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            zout.setLevel(9);
            
            ZipEntry metaEntry = new ZipEntry("metaEntry/");
            zout.putNextEntry(new ZipEntry(metaEntry));            
            ZipEntry dirEntry = new ZipEntry("dirEntry/");
            zout.putNextEntry(dirEntry);
            zipFile(zout, dirEntry, srcDir);
            try {
                
            } finally {
                zout.close();
            }
            
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        
    }
    
    /**
     * @param srcDir
     * @param out
     * @param verbose
     * @throws IOException
     * http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
     */
    public static void createZipFile(File srcDir, OutputStream out,
            boolean verbose) throws IOException {

        List<String> fileList = listDirectory(srcDir);
        ZipOutputStream zout = new ZipOutputStream(out);

        zout.setLevel(9);
        zout.setComment("Zipper v1.2");

        for (String fileName : fileList) {
            File file = new File(srcDir.getParent(), fileName);
            if (verbose)
                System.out.println("  adding: " + fileName);

            // Zip always use / as separator
            String zipName = fileName;
            if (File.separatorChar != '/')
                zipName = fileName.replace(File.separatorChar, '/');
            ZipEntry ze;
            if (file.isFile()) {
                ze = new ZipEntry(zipName);
                ze.setTime(file.lastModified());
                zout.putNextEntry(ze);
                FileInputStream fin = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                for (int n; (n = fin.read(buffer)) > 0;)
                    zout.write(buffer, 0, n);
                fin.close();
            } else {
                ze = new ZipEntry(zipName + '/');
                ze.setTime(file.lastModified());
                zout.putNextEntry(ze);
            }
        }
        zout.close();
    }

    public static List<String> listDirectory(File directory) throws IOException {

        Stack<String> stack = new Stack<String>();
        List<String> list = new ArrayList<String>();

        // If it's a file, just return itself
        if (directory.isFile()) {
            if (directory.canRead())
                list.add(directory.getName());
            return list;
        }

        // Traverse the directory in width-first manner, no-recursively
        String root = directory.getParent();
        stack.push(directory.getName());
        while (!stack.empty()) {
            String current = (String) stack.pop();
            File curDir = new File(root, current);
            String[] fileList = curDir.list();
            if (fileList != null) {
                for (String entry : fileList) {
                    File f = new File(curDir, entry);
                    if (f.isFile()) {
                        if (f.canRead()) {
                            list.add(current + File.separator + entry);
                        } else {
                            System.err.println("File " + f.getPath()
                                    + " is unreadable");
                            throw new IOException("Can't read file: "
                                    + f.getPath());
                        }
                    } else if (f.isDirectory()) {
                        list.add(current + File.separator + entry);
                        stack.push(current + File.separator + f.getName());
                    } else {
                        throw new IOException("Unknown entry: " + f.getPath());
                    }
                }
            }
        }
        return list;
    }
    
    
    void makeFile(File parent, String name){
        parent.mkdirs();
        TFUtils.printlnToFile(new File(parent, name), name);        
    }
    
    File prepareStructure(){
        File tmpFile = new File(getTfTestFolder(), "tmp");
        File structRoot = new File(tmpFile, "zip-struct");
        TFUtils.delTree(structRoot, true);
        structRoot.mkdirs();
        
        TFUtils.printlnToFile(new File(structRoot, "readme.txt"), "Experimental file");
        
        File dir1 = new File(structRoot, "dir1");
        makeFile(dir1, "file1-1");
        makeFile(dir1, "file1-2");
        File dir2 = new File(structRoot, "dir2");
        makeFile(dir2, "file2-1");
        makeFile(dir2, "file2-2");
        makeFile(dir1, "file1-1");
        File dir3 = new File(structRoot, "dir3");
        dir3.mkdirs();
        
        return structRoot;
    }
    
    public void testZipDbs() throws Exception{
        File tmpFolder = new File(getTfTestFolder(), "tmp");
        tmpFolder.mkdirs();
        FileOutputStream fos = new FileOutputStream(new File(tmpFolder, "dbs.zip"));
        zip(new File(getTfTestFolder(), "dbs"), fos);
        fos.close();
        
        
    }
    
    public void testZipFolders() throws Exception{
       
        File srcStruct = prepareStructure();

        File tmpFolder = new File(getTfTestFolder(), "tmp");
        tmpFolder.mkdirs();

        
        FileOutputStream fos = new FileOutputStream(new File(tmpFolder, "pr.zip"));
        zip(srcStruct, fos);
        fos.close();
        
        
    }
    
	@Override
	public void test() throws Exception {
		File tmpFile = new File(getTfTestFolder(), "tmp");
		File zipFile = new File(tmpFile, "pr.zip");
		tmpFile.mkdirs();
		{
			final int BUFFER = 2048;
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			// out.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[BUFFER];
			// get a list of files from current directory
			File files[] = (new File(getTfTestFolder())).listFiles();

			for (int i = 0; i < files.length; i++) {
				System.out.println("Adding: " + files[i]);
				File f = files[i];
				if(!f.isFile())continue;
				FileInputStream fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(TFTestCase.class.getCanonicalName() + "/" + files[i].getName());
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
			dest.close();
			
		}

	}

}
