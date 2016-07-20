package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.File;

import com.github.markusbernhardt.proxy.util.PlatformUtil;

/*****************************************************************************
 * Searches for Firefox profile on an OSX system. This will scan the
 * <i>Library/Application Support/Firefox</i> folder in the users home directory
 * to find the profiles.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

// TODO 02.06.2015 bros Format has changed in newer versions of firefox.

class OsxFirefoxProfileSource implements FirefoxProfileSource {

	/*************************************************************************
	 * Get profiles.ini for the Linux Firefox profile
	 ************************************************************************/

	@Override
	public File getProfilesIni() {
		File userDir = new File(PlatformUtil.getUserHomeDir());
		return new File(userDir, "Library" + File.separator + "Application Support" + File.separator + "Firefox"
		        + File.separator + "profiles.ini");
	}

}
