package com.github.markusbernhardt.proxy.util;

import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Defines some helper methods to find the correct platform.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class PlatformUtil {

    public static final String OVERRIDE_HOME_DIR = "com.btr.proxy.user.home";

    public enum Platform {
        WIN, LINUX, MAC_OS, SOLARIS, OTHER
    }

    public enum Desktop {
        WIN, KDE, GNOME, MAC_OS, OTHER
    }

    public enum Browser {
        IE, FIREFOX
    }

    /*************************************************************************
     * Gets the platform we are currently running on.
     * 
     * @return a platform code.
     ************************************************************************/

    public static Platform getCurrentPlattform() {
        String osName = System.getProperty("os.name");
        Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detecting platform. Name is: {0}", osName);

        if (osName.toLowerCase().contains("windows")) {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Windows platform: {0}", osName);
            return Platform.WIN;
        }
        if (osName.toLowerCase().contains("linux")) {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Linux platform: {0}", osName);
            return Platform.LINUX;
        }
        if (osName.startsWith("Mac OS")) {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Mac OS platform: {0}", osName);
            return Platform.MAC_OS;
        }
        if (osName.startsWith("SunOS")) {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Solaris platform: {0}", osName);
            return Platform.SOLARIS;
        }

        return Platform.OTHER;
    }

    /*************************************************************************
     * Gets the ID for the platform default browser.
     * 
     * @return a browser ID, null if no supported browser was detected.
     ************************************************************************/

    public static Browser getDefaultBrowser() {
        // Use better logic to detect default browser?
        if (getCurrentPlattform() == Platform.WIN) {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Browser is InternetExplorer");
            return Browser.IE;
        } else {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Browser Firefox. Fallback?");
            return Browser.FIREFOX;
        }
    }

    /*************************************************************************
     * Gets the desktop that we are running on.
     * 
     * @return the desktop identifier.
     ************************************************************************/

    public static Desktop getCurrentDesktop() {
        Platform platf = getCurrentPlattform();

        if (platf == Platform.WIN) {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Windows desktop");
            return Desktop.WIN;
        }
        if (platf == Platform.MAC_OS) {
            Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Mac OS desktop");
            return Desktop.MAC_OS;
        }

        if (platf == Platform.LINUX || platf == Platform.SOLARIS || platf == Platform.OTHER) {

            if (isKDE()) {
                Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected KDE desktop");
                return Desktop.KDE;
            }
            if (isGnome()) {
                Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Gnome desktop");
                return Desktop.GNOME;
            }
        }
        Logger.log(PlatformUtil.class, LogLevel.TRACE, "Detected Unknown desktop");
        return Desktop.OTHER;
    }

    /*************************************************************************
     * Checks if we are currently running under Gnome desktop.
     * 
     * @return true if it is a Gnome else false.
     ************************************************************************/

    private static boolean isGnome() {
        return System.getenv("GNOME_DESKTOP_SESSION_ID") != null;
    }

    /*************************************************************************
     * Checks if we are currently running under KDE desktop.
     * 
     * @return true if it is a KDE else false.
     ************************************************************************/

    private static boolean isKDE() {
        return System.getenv("KDE_SESSION_VERSION") != null;
    }

    /*************************************************************************
     * Gets the user home directory where normally all the settings are stored.
     * 
     * @return the path to the home directory.
     ************************************************************************/

    public static String getUserHomeDir() {
        String result = System.getProperty("user.home");
        String override = System.getProperty(OVERRIDE_HOME_DIR);
        if (override != null && override.trim().length() > 0) {
            result = override;
        }
        return result;
    }

}
