package com.github.markusbernhardt.autoproxy.search.desktop;

import java.net.ProxySelector;

import com.github.markusbernhardt.autoproxy.ProxySearchStrategy;
import com.github.markusbernhardt.autoproxy.search.desktop.gnome.GnomeProxySearchStrategy;
import com.github.markusbernhardt.autoproxy.search.desktop.kde.KdeProxySearchStrategy;
import com.github.markusbernhardt.autoproxy.search.desktop.osx.OsxProxySearchStrategy;
import com.github.markusbernhardt.autoproxy.search.desktop.win.WinProxySearchStrategy;
import com.github.markusbernhardt.autoproxy.util.Logger;
import com.github.markusbernhardt.autoproxy.util.PlatformUtil;
import com.github.markusbernhardt.autoproxy.util.ProxyException;
import com.github.markusbernhardt.autoproxy.util.Logger.LogLevel;
import com.github.markusbernhardt.autoproxy.util.PlatformUtil.Desktop;
import com.github.markusbernhardt.autoproxy.util.PlatformUtil.Platform;

/*****************************************************************************
 * This search provider will try to find out on which desktop platform we are
 * running and then will initialize the default proxy search.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class DesktopProxySearchStrategy implements ProxySearchStrategy {

    /*************************************************************************
     * Gets the default ProxySelector for the current platform.
     * 
     * @return a ProxySelector, null if none is found.
     * @throws ProxyException
     *             on error.
     ************************************************************************/

    public ProxySelector getProxySelector() throws ProxyException {
        ProxySearchStrategy strategy = findDesktopSpecificStrategy();
        return strategy == null ? null : strategy.getProxySelector();
    }

    /*************************************************************************
     * Determine the desktop and create a strategy for it.
     * 
     * @return a desktop specific strategy, null if none was found.
     ************************************************************************/

    private ProxySearchStrategy findDesktopSpecificStrategy() {
        Platform pf = PlatformUtil.getCurrentPlattform();
        Desktop dt = PlatformUtil.getCurrentDesktop();

        Logger.log(getClass(), LogLevel.TRACE, "Detecting system settings.");

        ProxySearchStrategy strategy = null;

        if (pf == Platform.WIN) {
            Logger.log(getClass(), LogLevel.TRACE, "We are running on Windows.");
            strategy = new WinProxySearchStrategy();
        } else if (dt == Desktop.KDE) {
            Logger.log(getClass(), LogLevel.TRACE, "We are running on KDE.");
            strategy = new KdeProxySearchStrategy();
        } else if (dt == Desktop.GNOME) {
            Logger.log(getClass(), LogLevel.TRACE, "We are running on Gnome.");
            strategy = new GnomeProxySearchStrategy();
        } else if (dt == Desktop.MAC_OS) {
            Logger.log(getClass(), LogLevel.TRACE, "We are running on Mac OSX.");
            strategy = new OsxProxySearchStrategy();
        }
        return strategy;
    }

}
