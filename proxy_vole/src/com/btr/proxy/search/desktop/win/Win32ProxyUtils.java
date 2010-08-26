package com.btr.proxy.search.desktop.win;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*****************************************************************************
 *  Defines the native methods used for windows to extract some system information.
 *  <p>
 *  This class will need some native code from the library proxy_util_"arch".dll.
 *  To load this library we use a three step algorithm as following:
 *  </p><P>  
 *  First check the System property "proxy_vole_lib_dir" if it is set and 
 *  it points to a folder where the dll is found than the dll from this 
 *  folder is loaded as e.g. <i>"proxy_vole_lib_dir"/proxy_util_w32.dll</i>
 *  </p><p>
 *  Second we try to load the dll from the subfolder <i>lib</i> if that one exists.<br> 
 *  Finally if we are inside of a jar file we need to extract the dll file
 *  to a temp-file because windows can not load dlls from a jar 
 *  directly. This is a hack but it may work.
 *  </p><p>
 *  Please note that the file is named Win32ProxyUtils but has now also support
 *  for x64 architecture.
 *  </p>
 *  @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class Win32ProxyUtils {
	
	public static final int WINHTTP_AUTO_DETECT_TYPE_DHCP  = 0x00000001;
	public static final int WINHTTP_AUTO_DETECT_TYPE_DNS_A = 0x00000002;

	private static String LIB_NAME_BASE = "proxy_util_";

	// Code for loading the windows native dll
	static {
		try {
			String LIB_NAME  = LIB_NAME_BASE + "w32.dll";
			if( !System.getProperty("os.arch").equals("x86") ) {
				LIB_NAME = LIB_NAME_BASE + System.getProperty("os.arch") + ".dll";
			}

			String libDir = System.getProperty("proxy_vole_lib_dir");
			if (libDir != null && new File(libDir, LIB_NAME).exists()) {
				System.load(new File(libDir, LIB_NAME).getAbsolutePath());
			} else  
			if (new File("lib", LIB_NAME).exists()) {
				System.load(new File("lib", LIB_NAME).getAbsolutePath());
			} else {
				InputStream source = Win32ProxyUtils.class.getResourceAsStream("/lib/"+LIB_NAME);
				File tempFile = File.createTempFile("proxy_vole", ".dll");
				tempFile.deleteOnExit();

				FileOutputStream fout = new FileOutputStream(tempFile);
				byte[] buffer = new byte[1024];
				int read = 0;
				while (read >= 0) {
					fout.write(buffer, 0, read);
					read = source.read(buffer);
				}
				fout.flush();
				fout.close();
				source.close();
				System.load(tempFile.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new RuntimeException("Error loading dll"+e.getMessage(), e); 
		} 
	}
	
	/*************************************************************************
	 * Constructor
	 ************************************************************************/
	
	public Win32ProxyUtils() {
		super();
	}
	
	/*************************************************************************
	 * WinHTTP method to detect an PAC URL.
	 * @param mode the mode to use.
	 * @return the PAC URL, null if none was found.
	 ************************************************************************/
	
	public native String winHttpDetectAutoProxyConfigUrl(int mode);

	/*************************************************************************
	 * Gets the default windows proxy settings.
	 * The returned string will have the following format. 
	 * TYPE PROXY | BYPASSLIST
	 * <p>
	 * e.g. DIRECT myproxy.mycompany.com:8080 | *.mycompany.com, localhost
	 * </p> 
	 * @return a string containing all info, null if not found.
	 ************************************************************************/
	// TODO Not implemented correctly in DLL yet.
	native String winHttpGetDefaultProxyConfiguration();

	/*************************************************************************
	 * Extracts the Internet Explorer proxy settings from the Windows system. 
	 * @return a data structure containing all details, null on fail.
	 ************************************************************************/
	
	public native Win32IESettings winHttpGetIEProxyConfigForCurrentUser();
	
	/*************************************************************************
	 * Extracts the Internet Explorer proxy settings from the Windows system. 
	 * @return a data structure containing all details, null on fail.
	 ************************************************************************/
	
	public native String readUserHomedir();
	
	
}

