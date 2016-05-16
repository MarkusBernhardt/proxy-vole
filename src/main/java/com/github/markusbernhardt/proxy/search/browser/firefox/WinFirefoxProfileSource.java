package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.File;
import java.io.IOException;

import com.github.markusbernhardt.proxy.search.desktop.win.Win32ProxyUtils;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

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
        return new Win32ProxyUtils().readUserHomedir();
    }

    /*************************************************************************
     * Get profiles.ini for the Windows Firefox profile
     * 
     * @throws IOException
     *             on error.
     ************************************************************************/

    public File getProfilesIni() throws IOException {

        File appDataDir = new File(getAppFolder());
        File cfgDir = new File(appDataDir, "Mozilla" + File.separator + "Firefox" + File.separator + "profiles.ini");
    }

}
