package com.github.markusbernhardt.proxy.search.browser.ie;

import java.net.ProxySelector;
import java.util.Properties;

import com.github.markusbernhardt.proxy.jna.win.WinHttp;
import com.github.markusbernhardt.proxy.jna.win.WinHttpCurrentUserIEProxyConfig;
import com.github.markusbernhardt.proxy.search.desktop.win.CommonWindowsSearchStrategy;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.sun.jna.platform.win32.WTypes.LPWSTR;
import com.sun.jna.platform.win32.WinDef.DWORD;

/*****************************************************************************
 * Extracts the proxy settings for Microsoft Internet Explorer. The settings are
 * read by invoking native Windows API methods.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class IEProxySearchStrategy extends CommonWindowsSearchStrategy {

	/*************************************************************************
	 * getProxySelector
	 * 
	 * @see com.github.markusbernhardt.proxy.ProxySearchStrategy#getProxySelector()
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() throws ProxyException {

		Logger.log(getClass(), LogLevel.TRACE, "Detecting IE proxy settings");

		IEProxyConfig ieProxyConfig = readIEProxyConfig();

		ProxySelector result = createPacSelector(ieProxyConfig);
		if (result == null) {
			result = createFixedProxySelector(ieProxyConfig);
		}
		return result;
	}

	/*************************************************************************
	 * Gets the printable name of the search strategy.
	 * 
	 * @return the printable name of the search strategy
	 ************************************************************************/

	@Override
	public String getName() {
		return "IE";
	}

	/*************************************************************************
	 * Loads the settings from the windows registry.
	 * 
	 * @return WinIESettings containing all proxy settings.
	 ************************************************************************/

	public IEProxyConfig readIEProxyConfig() {

		// Retrieve the IE proxy configuration.
		WinHttpCurrentUserIEProxyConfig winHttpCurrentUserIeProxyConfig = new WinHttpCurrentUserIEProxyConfig();
		boolean result = WinHttp.INSTANCE.WinHttpGetIEProxyConfigForCurrentUser(winHttpCurrentUserIeProxyConfig);
		if (!result) {
			return null;
		}

		// Create IEProxyConfig instance
		return new IEProxyConfig(winHttpCurrentUserIeProxyConfig.fAutoDetect,
		        winHttpCurrentUserIeProxyConfig.lpszAutoConfigUrl != null
		                ? winHttpCurrentUserIeProxyConfig.lpszAutoConfigUrl.getValue() : null,
		        winHttpCurrentUserIeProxyConfig.lpszProxy != null ? winHttpCurrentUserIeProxyConfig.lpszProxy.getValue()
		                : null,
		        winHttpCurrentUserIeProxyConfig.lpszProxyBypass != null
		                ? winHttpCurrentUserIeProxyConfig.lpszProxyBypass.getValue() : null);

	}

	/*************************************************************************
	 * Parses the settings and creates an PAC ProxySelector for it.
	 * 
	 * @param ieSettings
	 *            the IE settings to use.
	 * @return a PacProxySelector the selector or null.
	 ************************************************************************/

	private PacProxySelector createPacSelector(IEProxyConfig ieProxyConfig) {
		String pacUrl = null;

		if (ieProxyConfig.isAutoDetect()) {
			Logger.log(getClass(), LogLevel.TRACE, "Autodetecting script URL.");
			// This will take some time.
			DWORD dwAutoDetectFlags = new DWORD(
			        WinHttp.WINHTTP_AUTO_DETECT_TYPE_DHCP | WinHttp.WINHTTP_AUTO_DETECT_TYPE_DNS_A);
			LPWSTR ppwszAutoConfigUrl = new LPWSTR();
			boolean result = WinHttp.INSTANCE.WinHttpDetectAutoProxyConfigUrl(dwAutoDetectFlags, ppwszAutoConfigUrl);
			if (result) {
				pacUrl = ppwszAutoConfigUrl.getValue();
			}
		}
		if (pacUrl == null) {
			pacUrl = ieProxyConfig.getAutoConfigUrl();
		}
		if (pacUrl != null && pacUrl.trim().length() > 0) {
			Logger.log(getClass(), LogLevel.TRACE, "IE uses script: " + pacUrl);

			// Fix for issue 9
			// If the IE has a file URL and it only starts has 2 slashes,
			// add a third so it can be properly converted to the URL class
			if (pacUrl.startsWith("file://") && !pacUrl.startsWith("file:///")) {
				pacUrl = "file:///" + pacUrl.substring(7);
			}
			return ProxyUtil.buildPacSelectorForUrl(pacUrl);
		}

		return null;
	}

	/*************************************************************************
	 * Parses the proxy settings into an ProxySelector.
	 * 
	 * @param ieSettings
	 *            the settings to use.
	 * @return a ProxySelector, null if no settings are set.
	 * @throws ProxyException
	 *             on error.
	 ************************************************************************/

	private ProxySelector createFixedProxySelector(IEProxyConfig ieProxyConfig) throws ProxyException {
		String proxyString = ieProxyConfig.getProxy();
		String bypassList = ieProxyConfig.getProxyBypass();
		if (proxyString == null) {
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, "IE uses manual settings: {0} with bypass list: {1}", proxyString,
		        bypassList);

		Properties p = parseProxyList(proxyString);

		ProtocolDispatchSelector ps = buildProtocolDispatchSelector(p);

		ProxySelector result = setByPassListOnSelector(bypassList, ps);
		return result;
	}

}
