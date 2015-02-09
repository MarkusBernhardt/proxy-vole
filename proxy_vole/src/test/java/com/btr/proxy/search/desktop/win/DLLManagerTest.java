package com.btr.proxy.search.desktop.win;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import com.btr.proxy.TestUtil;

/*****************************************************************************
 * Unit tests for DLL loading code.  
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class DLLManagerTest {

	/*************************************************************************
	 * Reset system property at the end.
	 ************************************************************************/
	@AfterClass
	public static void teardown() {
		System.setProperty(DLLManager.LIB_DIR_OVERRIDE, "");
	}
	
	/*************************************************************************
	 * Test method 
	 * @throws IOException on error 
	 ************************************************************************/
	@Test
	public void testFindLibFileOverride() throws IOException {
		String path = TestUtil.TEST_DATA_FOLDER+"win";
		System.setProperty(DLLManager.LIB_DIR_OVERRIDE, path);
		File actual = DLLManager.findLibFile();
		assertTrue(actual.getAbsolutePath().contains(path));
	}
	
	/*************************************************************************
	 * Test method 
	 * @throws IOException on error 
	 ************************************************************************/
	@Test
	public void testFindLibFileDefault() throws IOException {
		System.setProperty(DLLManager.LIB_DIR_OVERRIDE, "");
		File actual = DLLManager.findLibFile();
		assertTrue(actual.getAbsolutePath().contains(File.separator+"proxy_vole"));
	}

	/*************************************************************************
	 * Test method 
	 * @throws IOException on error 
	 ************************************************************************/
	@Test
	@Ignore // We now only clean files older than one day. Issue 52
	public void testCleanupTempFiles() throws IOException {
		File f1 = File.createTempFile(DLLManager.TEMP_FILE_PREFIX+"_ABC", DLLManager.DLL_EXTENSION);
		assertTrue(f1.exists());
		DLLManager.cleanupTempFiles();
		assertFalse(f1.exists());
	}
	
	/*************************************************************************
	 * Test method 
	 * @throws IOException on error 
	 ************************************************************************/
	@Test
	public void testFileCopy() throws IOException {
		File originalFile = new File(TestUtil.TEST_DATA_FOLDER+"win"+File.separator+"proxy_util_w32.dll");
		File tempFile = File.createTempFile(DLLManager.TEMP_FILE_PREFIX, DLLManager.TEMP_FILE_PREFIX);
		DLLManager.copy(new FileInputStream(originalFile), new FileOutputStream(tempFile));
		assertTrue(tempFile.exists() && tempFile.length() == originalFile.length());
		tempFile.delete();
	}

}

