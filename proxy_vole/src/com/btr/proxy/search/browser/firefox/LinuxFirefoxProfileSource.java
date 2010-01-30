package com.btr.proxy.search.browser.firefox;

import java.io.File;

import com.btr.proxy.util.Logger;
import com.btr.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Searches for Firefox profile on an Linux / Unix base system.
 * This will scan the <i>.mozilla</i> folder in the users home directory to find the 
 * profiles. 
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

class LinuxFirefoxProfileSource implements FirefoxProfileSource {

	/*************************************************************************
	 * Get profile folder for the Linux Firefox profile
	 ************************************************************************/
	
	public File getProfileFolder() {
		File userDir = new File(System.getProperty("user.home"));
		File cfgDir = new File(userDir, ".mozilla"+File.separator+"firefox"+File.separator);
		if (!cfgDir.exists()) {
			Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder not found!");
			return null;
		}
		File[] profiles = cfgDir.listFiles();
		if (profiles == null || profiles.length == 0) {
			Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder not found!");
			return null;
		}
		for (File p : profiles) {
			if (p.getName().endsWith(".default")) {
				Logger.log(getClass(), LogLevel.TRACE, "Firefox settings folder is {0}", p);
				return p;
			}
		}
		
		// Fall back -> take the first one found.
		Logger.log(getClass(), LogLevel.TRACE, "Firefox settings folder is {0}", profiles[0]);
		return profiles[0];
	}

}
