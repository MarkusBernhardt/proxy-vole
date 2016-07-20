package com.github.markusbernhardt.proxy.search.desktop;

import java.net.ProxySelector;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.desktop.gnome.GnomeProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.desktop.kde.KdeProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.desktop.osx.OsxProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.desktop.win.WinProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.PlatformUtil.Desktop;
import com.github.markusbernhardt.proxy.util.PlatformUtil.Platform;

/*****************************************************************************
 * This search provider will try to find out on which desktop platform we are
 * running and then will initialize the default proxy search.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class DesktopProxySearchStrategy implements ProxySearchStrategy {

	/*************************************************************************
	 * Gets the default ProxySelector for the current platform.
	 * 
	 * @return a ProxySelector, null if none is found.
	 * @throws ProxyException
	 *             on error.
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() throws ProxyException {
		ProxySearchStrategy strategy = findDesktopSpecificStrategy();
		return strategy == null ? null : strategy.getProxySelector();
	}

	/*************************************************************************
	 * Gets the printable name of the search strategy.
	 * 
	 * @return the printable name of the search strategy
	 ************************************************************************/

	@Override
	public String getName() {
		return "desktop";
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
