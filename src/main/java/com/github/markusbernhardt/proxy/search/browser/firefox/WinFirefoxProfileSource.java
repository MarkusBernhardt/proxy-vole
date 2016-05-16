package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.File;
import java.io.IOException;

import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;

/*****************************************************************************
 * Finds the Firefox profile on Windows platforms. On Windows the profiles are
 * located in the users appdata directory under:
 * <p>
 * <i>Mozilla\Firefox\Profiles\</i>
 * </p>
 * The location of the appdata folder is read from the windows registry.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

// TODO 02.06.2015 bros Format has changed in newer versions of firefox.

class WinFirefoxProfileSource implements FirefoxProfileSource {

    /*************************************************************************
     * Constructor
     ************************************************************************/

    public WinFirefoxProfileSource() {
        super();
    }

    /*************************************************************************
     * Reads the current location of the app data folder from the registry.
     * 
     * @return a path to the folder.
     ************************************************************************/

    private String getAppFolder() {
        return Shell32Util.getFolderPath(ShlObj.CSIDL_APPDATA);
    }

    /*************************************************************************
     * Get profiles.ini for the Windows Firefox profile
     * 
     * @throws IOException
     *             on error.
     ************************************************************************/

    @Override
    public File getProfilesIni() throws IOException {
        File appDataDir = new File(getAppFolder());
        return new File(appDataDir, "Mozilla" + File.separator + "Firefox" + File.separator + "profiles.ini");
    }

}
