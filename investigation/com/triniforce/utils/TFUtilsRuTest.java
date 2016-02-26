/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.triniforce.db.test.TFTestCase;

public class TFUtilsRuTest extends TFTestCase {
	@Override
	public void test() throws Exception {
		File ruFolder = new File(getTfTestFolder(), "ru");
		File out = new File(getTfTestFolder(), "ruout.zip");
		TFUtils.zipFolder(ruFolder, out);
	}

	public void testSimple() throws Exception {
		File ruFolder = new File(getTfTestFolder(), "ru");
		File out = new File(getTfTestFolder(), "ruout.zip");

		String zipFile = out.getAbsolutePath();
		String sourceDirectory = ruFolder.getAbsolutePath();

		// create byte buffer
		byte[] buffer = new byte[1024];

		// create object of FileOutputStream
		FileOutputStream fout = new FileOutputStream(zipFile);

		// create object of ZipOutputStream from FileOutputStream
		ZipOutputStream zout = new ZipOutputStream(fout);
		
		try {
			// create File object from directory name
			File dir = new File(sourceDirectory);

			// check to see if this directory exists
			if (!dir.isDirectory()) {
				System.out.println(sourceDirectory + " is not a directory");
				return;
			}

			File[] files = dir.listFiles();

			for (int i = 0; i < files.length; i++) {
				System.out.println("Adding " + files[i].getName());

				// create object of FileInputStream for source file
				FileInputStream fin = new FileInputStream(files[i]);

				zout.putNextEntry(new ZipEntry(files[i].getName()));

				int length;

				while ((length = fin.read(buffer)) > 0) {
					zout.write(buffer, 0, length);
				}
				zout.closeEntry();

				// close the InputStream
				fin.close();
			}

			// close the ZipOutputStream
		} finally {
			fout.close();
			zout.close();
		}

		System.out.println("Zip file has been created!");
	}

}
