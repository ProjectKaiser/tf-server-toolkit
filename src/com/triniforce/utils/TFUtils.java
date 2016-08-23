/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TFUtils {
	
	public interface IAssertParams{
		void equal(Object... to);
	}
	
	public static final String UTF8 = "UTF-8";
	
	public static String lineSeparator(){
		return System.getProperty("line.separator");
	}
	
	public static String bytesToString(byte bytes[]){
		if(null == bytes){
			return null;
		}
		try {
			return new String(bytes, UTF8);
		} catch (UnsupportedEncodingException e) {
			return new String(bytes);
		}
	}
	
	public static byte[] stringToBytes(String str){
		if(null == str){
			return null;
		}
		try {
			return str.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			return str.getBytes();
		}
	}
	
	/**
	 * @param language
	 * @param country
	 * @param variant
	 * @return Locale.getDefault() if all parameters are null
	 */
	public static Locale constructLocale(Object language, Object country, Object variant){
	    if( language != null && country != null &&  variant != null){
	        return new Locale((String)language, (String)country, (String)variant);            
	    }
	    if( language != null && country != null){
	        return new Locale((String)language, (String)country);            
	    }
	    if( language != null){
	        return new Locale((String)language);            
	    }        
	    return Locale.getDefault();        
	}

	public static boolean isEmptyString(String s) {
	    if (null == s)
	        return true;
	    if (s.length() == 0)
	        return true;
	    return false;
	}

	public static Short asShort(Object value){
	    if( null == value )return null;
        if( value instanceof Number){
            return ((Number)value).shortValue();
        }
        if( value instanceof String){
            return Short.parseShort((String) value);
        }

	    return (Short)value;
	}

	public static Integer asInteger(Object value){
	    if( null == value )return null;
	    if( value instanceof Number){
	        return ((Number)value).intValue();
	    }
	    if( value instanceof String){
            return Integer.parseInt((String) value);
        }
	    TFUtils.assertTrue(false, "Unknown type for asInteger " + value.getClass());
	    return 0;
	}
	
	public static String toString(Object o){
		if(null == o) return "null";
		return o.toString();
	}
	
	public static Long asLong(Object value){
	    if( null == value )return null;
        if( value instanceof Number){
            return ((Number)value).longValue();
        }
	    if( value instanceof String){
            return Long.parseLong((String) value);
        }
	    TFUtils.assertTrue(false, "Unknown type for asLong " + value.getClass());
	    return 0L;
	}

	public static boolean equals(Object expected, Object actual) {
	    if (expected == actual)
	        return true;
	    if (expected == null || actual == null)
	        return false;
	    
	    if(expected instanceof Number){
	        if(! (actual instanceof Number)){
	            if(! (actual instanceof String)){
	                return false;
	            }else{
	                try{
	                    actual = Long.parseLong((String) actual);
	                }catch(Throwable t){
	                    ApiAlgs.getLog(TFUtils.class).info("parseLong() error: " + actual);
	                    return false;
	                }
	            }
	        }
	        return TFUtils.asLong(expected).equals(TFUtils.asLong(actual));
	    }
	    
	    return expected.equals(actual);
	}

	public static class EZipEntryNotFound extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EZipEntryNotFound(ZipFile zf, String entryName) {
            super(MessageFormat.format("Entry \"{1}\" not found in zip \"{0}\"", zf.getName(), entryName));
        }
    }
	
	public static ZipFile unzipOpen(File f) {
		try {
			return new ZipFile(f);
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}
		return null;
	}
	
	public static void unzipClose(ZipFile zf){
	    if( null == zf) return;
		try {
			zf.close();
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}
	}
	
	public static String unzipString(ZipFile zf, String entryName, boolean throwIfNotFound) {
	    ZipEntry entry = zf.getEntry(entryName);
	    if( null == entry){
	        if(throwIfNotFound){
	            throw new EZipEntryNotFound(zf, entryName);
	        }else{
	          return null;  
	        }
	    }
        try {
            InputStream is = zf.getInputStream(entry);
            return readStringFromStream(is, "utf-8", true);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }
	
	public static void unzipEntry(ZipFile zf, String astartEntryPattern, File outputFolder){
		String startEntryPattern = zipMakeDirEntryName(astartEntryPattern);
//	    assertTrue(startEntryPattern.endsWith("/"), "Entry pattern must end with '/': "
//                + startEntryPattern);
        for (Enumeration<? extends ZipEntry> e = zf.entries(); e
                .hasMoreElements();) {
            ZipEntry entry = e.nextElement();
            if (!entry.getName().startsWith(startEntryPattern)){
                continue;
            }
            ApiAlgs.getLog(TFUtils.class).trace(entry.getName());
            String relativeEntryName = entry.getName().substring(startEntryPattern.length());
            String relativeFileName = relativeEntryName.replace("/", File.separator);
            if( 0 == relativeFileName.length()){
                continue;
            }
            if(relativeFileName.equals(File.separator)){
                continue;
            }
            File f = new File(outputFolder, relativeFileName);
            File parent = f.getParentFile();
            if( null == parent){
                continue;
            }
            if( entry.isDirectory()){
                f.mkdirs();
                f.setLastModified(entry.getTime());
                continue;
            }
            parent.mkdirs();
            try {
                InputStream in = zf.getInputStream(entry);
                try {
                    copyStream(in, f);
                } finally {
                    in.close();
                }
                f.setLastModified(entry.getTime());
            } catch (Exception ex) {
                ApiAlgs.rethrowException(ex);
            }
        }
	}
	
	public static void copyStream(InputStream in, OutputStream out) {
	    try {
	        byte[] buffer = new byte[1024];
            while (true) {
                int readCount = in.read(buffer);
                if (readCount < 0) {
                    break;
                }
                out.write(buffer, 0, readCount);
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

	 public static void copyStream(File file, OutputStream out) {
        try {
            InputStream in = new FileInputStream(file);
            try {
                copyStream(in, out);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

    public static void copyStream(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                copyStream(in, out);
            } finally {
                out.close();
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }
	
    
    public static ZipOutputStream zipOpen(File zipFile){
    	try {
    		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
    		zos.setLevel(9);
    		return zos;
	    } catch (Exception e) {
	        ApiAlgs.rethrowException(e);
	    }
	    return null;
    }
    
    public static void zipClose(ZipOutputStream zos){
        if( null == zos)return;
    	try {
    		zos.close();
	    } catch (Exception e) {
	        ApiAlgs.rethrowException(e);
	    }
    }
    
    
	public static void zipFolder(File folder, File zipFile){
	    try {
	        ZipOutputStream zout = zipOpen(zipFile);
	        try {
	            zipFolderToEntry(zout, null, folder);
	        } finally {
	            zout.close();
	        }
	    } catch (Exception e) {
	        ApiAlgs.rethrowException(e);
	    }        
	}

	public static void zipString(ZipOutputStream zout, String entryName, String data){
	       try {
	            zout.putNextEntry(new ZipEntry(entryName));
	            zout.write(data.getBytes("utf-8"));
	        } catch (Exception e) {
	            ApiAlgs.rethrowException(e);
	        }
	}
	
	public static void zipFile(ZipOutputStream zout, ZipEntry fileEntry, File file) {
		try {
			zout.putNextEntry(fileEntry);
			long sz = 0;
			FileInputStream fin = new FileInputStream(file);
			try {
				byte[] buffer = new byte[4096];
				for (int n; (n = fin.read(buffer)) > 0;) {
					zout.write(buffer, 0, n);
					sz += n;
				}
			} finally {
				fin.close();
			}
			fileEntry.setSize(sz);
            zout.closeEntry();
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}
	}
	
	public static String zipMakeDirEntryName(String entry){
		if( 0 == entry.length()) return entry;
		if(!entry.endsWith("/")){
			return entry + "/";
		} else {
			return entry;
		}
	}
	
	public static void zipFolderToEntryName(ZipOutputStream zout, String entry, File srcDir){
		zipFolderToEntry(zout, new ZipEntry(zipMakeDirEntryName(entry)), srcDir);
	}
	
	
	public static void zipFolderToEntry(ZipOutputStream zout, ZipEntry entry, File srcDir){
	    try {
	    	if(null!= entry){
	    		TFUtils.assertTrue(entry.isDirectory(), "Entry is not directory " + entry.getName());
	    		zout.putNextEntry(entry);
	    		zout.closeEntry();
	    	}
	    	TFUtils.assertTrue(srcDir.isDirectory(), "File \"" + srcDir + "\" is not directory");
	    	String entryName = null != entry? entry.getName():"";
            entryName = zipMakeDirEntryName(entryName);
	        for (File f : srcDir.listFiles()) {
	            String zipEntryName = f.isFile() ? entryName
	                    + f.getName() : entryName + f.getName() + "/";
	            ZipEntry fileEntry = new ZipEntry(zipEntryName);
	            fileEntry.setTime(f.lastModified());	            
	            if (f.isDirectory()) {
	                zipFolderToEntry(zout, fileEntry, f);
	            } else {
	            	zipFile(zout, fileEntry, f);
	            }
	        }
	    } catch (Exception e) {
	        ApiAlgs.rethrowException(e);
	    }        
	}

	/**
	 * 
	 * File management utilities 
	 * 
	 */
	
	public static void delTree(File dir, boolean deleteDirItSelf) {
	    if (!dir.exists())
	        return;
	    for (File subFile : dir.listFiles()) {
	        // String name = subFile.getName();
	        if (subFile.isDirectory()) {
	            delTree(subFile, true);
	        }
	        subFile.delete();
	    }
	    if (deleteDirItSelf) {
	        dir.delete();
	    }
	}

    public static List<String> readLinesFromFile(File f, int n) {
        TFUtils.assertTrue(n >=0, "Wrong arg:" + n);
        List<String> res = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(f);
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(fis, "utf8"));
                try{
                    String line;
                    while ((line = br.readLine()) != null && res.size() < n) {
                        res.add(line);
                    }
                }
                finally{
                    br.close();
                }
            }finally{
                fis.close();
            }
            return res;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return res;
    }
    
    public static List<String> readLastLinesFromFile(File f, int n, int approxLineSize) {
        TFUtils.assertTrue(n >=0, "Wrong arg:" + n);
        TFUtils.assertTrue(approxLineSize >=0, "Wrong arg:" + approxLineSize);
        List<String> res1 = new ArrayList<String>();
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            try{
            long endPos = raf.length();
            int dataSize  = ((approxLineSize + 2)*(n*6/4 + 10)); //space reservation
            boolean isShort = false;
            if(dataSize>=endPos){
                dataSize = (int) endPos;
                isShort = true;
            }
            raf.seek(endPos - dataSize);            
            byte data[] = new byte[dataSize];
            raf.readFully(data);
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            BufferedReader br = new BufferedReader(new InputStreamReader(bis, "utf8"));
            String line;
            while ((line = br.readLine()) != null){
                res1.add(line);
            }
            if(!isShort && res1.size() > 1){
                res1.remove(0);//since it is not fully read
            }
            
            }finally{
                raf.close();
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        ListIterator<String> li = res1.listIterator(res1.size()> n? res1.size()- n: 0);
        List<String> res = new ArrayList<String>();
        while (li.hasNext()) {
            res.add(li.next());
        }                
        return res;
    }
	
	public static void printlnToFile(File f, String s) {
	    printlnToFile(f, s, false);
	       
	}
	
	public static void printlnToFile(File f, String s, boolean append) {
	    try {
	        FileOutputStream fos = new FileOutputStream(f, append);
	        PrintStream ps = new PrintStream(fos, true, "utf8");
	        try {
	            ps.println(s);
	        } finally {
	            ps.close();
	            fos.close();
	        }
	    } catch (Exception e) {
	        ApiAlgs.rethrowException(e);
	    }
	}

	//public static String readLinesFromFile(File file, String charset, boolean closeStream){
	
	public static String readStringFromStream(InputStream is, String charset, boolean closeStream){
		StringBuffer res = new StringBuffer();
		try {
			char buf[] = new char[4096];
			try {
				InputStreamReader isReader = new InputStreamReader(is, charset);
				int nRead;
				while ((nRead = isReader.read(buf)) > 0) {
					res.append(buf, 0, nRead);
				}
			} finally {
				if (closeStream) {
					is.close();
				}
			}
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		}
		return res.toString();
	}
	
	/**
	 * @param cls Class reousrce will be read from
	 * @param name Resource name
	 * @return String with resource content 
	 * @throws EUtils.EResourceNotFound
	 */
	public static String readResource(Class cls, String resourceName) throws EUtils.EResourceNotFound{
	    StringBuffer res = new StringBuffer();
	    char buf[] = new char[256];
	    try {
	        InputStream is = cls.getResourceAsStream(resourceName);
	        if( null == is) throw new EUtils.EResourceNotFound(cls, resourceName);            
	        InputStreamReader iReader = new InputStreamReader(is, "utf-8");
	        BufferedReader br = new BufferedReader(iReader);
	        int nRead;
	        while ((nRead = br.read(buf)) > 0) {
	            res.append(buf, 0, nRead);
	        }
	    } catch (IOException e) {
	        ApiAlgs.rethrowException(e);
	    }
	    return res.toString();
	}

    public static void assertNotNull(Object value, String name){
        if(null != value) return;
        throw new EUtils.EAssertNotNullFailed(MessageFormat.format("Unexpected null value for \"{0}\"",name)); //$NON-NLS-1$
    }

    public static String msgPrefix(String prefix){
    	return TFUtils.isEmptyString(prefix)?"" :prefix +": ";
    }
    
    public static void assertEqualArrays(String prefix, Object expected[], Object actual[]){
    	if(expected.length != actual.length){
    		throw new EUtils.EAssertEqualsFailed(msgPrefix(prefix) + "different size", expected.length, actual.length);    		
    	}
		for (int idx = 0; idx < expected.length; idx++) {
			assertEquals(prefix + "[" + idx+"]", expected[idx], actual[idx]);
		}
    }
    
    public static IAssertParams assertParams(final Object... aexpected){
    	final Object expected = (aexpected != null && aexpected.length == 1)?aexpected[0]: aexpected;
    	IAssertParams res = new IAssertParams() {
			@Override
			public void equal(Object... to) {
				Object actual = (to != null && to.length == 1)?to[0]: to;
				assertEquals(expected, actual);
				
			}
		};
		return res;
    	
    }
    
    public static void assertEquals(String prefix, Object expected, Object actual){
        if( expected == actual ) return;
        if( expected == null || actual == null){
            throw new EUtils.EAssertEqualsFailed(prefix, expected, actual);
        }
        if(expected instanceof List){
        	expected = ((List)expected).toArray();
        }
        if(actual instanceof List){
        	actual  = ((List)actual).toArray();
        }        
        if(expected instanceof Object[]){
        	if(! (actual instanceof Object[])){
        		throw new EUtils.EAssertEqualsFailed("Array vs not array", expected.getClass(), actual.getClass());        		
        	}
        	assertEqualArrays(prefix, (Object[])expected, (Object[])actual);
        	return;
        }
        if(TFUtils.equals(expected, actual))return;
        throw new EUtils.EAssertEqualsFailed(prefix, expected, actual);
    }
    
    
    public static void assertEquals(Object expected, Object actual){
    	assertEquals("", expected, actual);
    }

    public static void assertTrue(boolean expr, String msg){
        if(!expr){
            throw new EUtils.EAssertTrueFailed(msg);
        }
    }
    
    public static List list(Object... args){
    	return Arrays.asList(args);
    }
    
	public static Map<String, Object> arrayToMap(final Object[] namesValues) {
		// odd number of parameters
		if ((namesValues.length & 0x1) == 1)
			throw new IllegalArgumentException("value"); //$NON-NLS-1$

		return new AbstractMap<String, Object>() {

			@Override
			public Set<java.util.Map.Entry<String, Object>> entrySet() {
				return new AbstractSet<java.util.Map.Entry<String, Object>>() {
					@Override
					public Iterator<java.util.Map.Entry<String, Object>> iterator() {
						return new Iterator<java.util.Map.Entry<String, Object>>() {
							int current = 0;

							public boolean hasNext() {
								return current < namesValues.length;
							}

							public java.util.Map.Entry<String, Object> next() {
								final int idx = current;
								current += 2;
								return new java.util.Map.Entry<String, Object>() {
									public String getKey() {
										Object name = namesValues[idx];
										String strName = null;
										if (name instanceof String)
											strName = (String) name;
										else if (name instanceof IName)
											strName = ((IName) name).getName();
										else if (name instanceof Class)
											strName = ((Class) name).getName();
										else
											throw new IllegalArgumentException("name"); //$NON-NLS-1$
										return strName;
									}

									public Object getValue() {
										return namesValues[idx + 1];
									}

									public Object setValue(Object arg0) {
										throw new RuntimeException("not impelmented"); //$NON-NLS-1$
									}
								};
							}

							public void remove() {
								throw new RuntimeException("not impelmented"); //$NON-NLS-1$
							}
						};
					}

					@Override
					public int size() {
						return namesValues.length / 2;
					}
				};
			}
		};
	}
    

}
