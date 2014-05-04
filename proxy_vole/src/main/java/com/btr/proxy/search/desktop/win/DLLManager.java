package com.btr.proxy.search.desktop.win;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.btr.proxy.util.Logger;
import com.btr.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * This class provides some helper methods to work with the dll 
 * extracting /loading for windows.
 *  
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

final class DLLManager {

	private static final class TempDLLFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			String name = pathname.getName();
			return pathname.isFile() && 
				name.startsWith(TEMP_FILE_PREFIX) && 
				name.endsWith(DLL_EXTENSION);
		}
	}

	public static final String LIB_DIR_OVERRIDE = "proxy_vole_lib_dir";
	
	static final String TEMP_FILE_PREFIX = "proxy_vole";
	static final String DLL_EXTENSION = ".dll";
	static String LIB_NAME_BASE = "proxy_util_";
	static final String DEFAULT_LIB_FOLDER = "lib";
	
	/*************************************************************************
	 * Find the location of the native code dll file.
	 * @return the File pointing to the dll.
	 * @throws IOException on IO error.
	 ************************************************************************/
	
	static File findLibFile() throws IOException {
		String libName = buildLibName();
		File libFile = getOverrideLibFile(libName);
		if (libFile == null || libFile.exists() == false) {
			libFile = getDefaultLibFile(libName);
		} 
		if (libFile == null || libFile.exists() == false) {
			libFile = extractToTempFile(libName);
		}
		return libFile;
	}

	/*************************************************************************
	 * Delete old temp files that may be there because they were extracted
	 * from the jar but could not be deleted on VM shutdown because they
	 * are still locked by windows.
	 * This is here to prevent a lot of temp dll files on disk.
	 ************************************************************************/
	
	static void cleanupTempFiles() {
		try {
			String tempFolder = System.getProperty("java.io.tmpdir");
			if (tempFolder == null || tempFolder.trim().length() == 0) { 
				return;
			}
			File fldr = new File(tempFolder);
			File[] oldFiles = fldr.listFiles(new TempDLLFileFilter());
			if (oldFiles == null) {
				return;
			}
			for (File tmp : oldFiles) {
				tmp.delete();
			}
		} catch (Exception e) {
			Logger.log(DLLManager.class, LogLevel.DEBUG, "Error cleaning up temporary dll files. ", e);
		}
	}

	/*************************************************************************
	 * @param libName
	 * @return  
	 ************************************************************************/
	
	private static File getDefaultLibFile(String libName) {
		return new File(DEFAULT_LIB_FOLDER, libName);
	}
	
	/*************************************************************************
	 * Gets the file name that was overriden via system property.
	 * @param libName
	 * @return the file, null if it is not existing.
	 ************************************************************************/
	
	private static File getOverrideLibFile(String libName) {
		String libDir = System.getProperty(LIB_DIR_OVERRIDE);
		if (libDir == null || libDir.trim().length() == 0) {
			return null;
		}
		return new File(libDir, libName);
	}

	/*************************************************************************
	 * @param libName
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 ************************************************************************/
	
	static File extractToTempFile(String libName) throws IOException {
		InputStream source = Win32ProxyUtils.class.getResourceAsStream("/"+DEFAULT_LIB_FOLDER+"/"+libName);
		File tempFile = File.createTempFile(TEMP_FILE_PREFIX, DLL_EXTENSION);
		tempFile.deleteOnExit();
		FileOutputStream destination = new FileOutputStream(tempFile);
		copy(source, destination);
		return tempFile;
	}

	/*************************************************************************
	 * @param c a closeable to cleanup ignoring all errors.
	 ************************************************************************/
	
	private static void closeStream(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
			// Ignore cleanup errors
		}
	}

	/*************************************************************************
	 * Copies the content from source to destination.
	 * @param source
	 * @param dest
	 * @throws IOException
	 ************************************************************************/
	
	static void copy(InputStream source, OutputStream dest)
			throws IOException {
		try {
			byte[] buffer = new byte[1024];
			int read = 0;
			while (read >= 0) {
				dest.write(buffer, 0, read);
				read = source.read(buffer);
			}
			dest.flush();
		} finally {
			closeStream(source);
			closeStream(dest);
		}
	}

	/*************************************************************************
	 * @return the name of the dll valid for the current architecture.
	 ************************************************************************/
	
	private static String buildLibName() {
		String arch = "w32";
		if(!System.getProperty("os.arch").equals("x86") ) {
			arch = System.getProperty("os.arch");
		}
		return LIB_NAME_BASE + arch + DLL_EXTENSION;
	}
	
}

