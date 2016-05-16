package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.File;
import java.io.IOException;

/*****************************************************************************
 * A profile source for Firefox profiles.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
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
