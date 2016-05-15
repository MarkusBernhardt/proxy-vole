package com.github.markusbernhardt.autoproxy.search.browser.firefox;

import java.io.File;

import com.github.markusbernhardt.autoproxy.util.Logger;
import com.github.markusbernhardt.autoproxy.util.PlatformUtil;
import com.github.markusbernhardt.autoproxy.util.Logger.LogLevel;

/*****************************************************************************
 * Searches for Firefox profile on an OSX system. This will scan the
 * <i>Library/Application Support/Firefox</i> folder in the users home directory
 * to find the profiles.
 *
 * @author Markus Bernhardt Copyright 2016
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

// TODO 02.06.2015 bros Format has changed in newer versions of firefox.

class OsxFirefoxProfileSource implements FirefoxProfileSource {

    /*************************************************************************
     * Get profile folder for the Linux Firefox profile
     ************************************************************************/

    public File getProfileFolder() {
        File userDir = new File(PlatformUtil.getUserHomeDir());
        File cfgDir = new File(userDir, "Library" + File.separator + "Application Support" + File.separator + "Firefox"
                + File.separator + "Firefox" + File.separator);
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
