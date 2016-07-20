package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.File;
import java.io.IOException;

/*****************************************************************************
 * A profile source for Firefox profiles.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

interface FirefoxProfileSource {

	/*************************************************************************
	 * Gets the profiles.ini file found on the current system.
	 * 
	 * @return the config folder.
	 * @throws IOException
	 *             on error.
	 ************************************************************************/

	public File getProfilesIni() throws IOException;

}
