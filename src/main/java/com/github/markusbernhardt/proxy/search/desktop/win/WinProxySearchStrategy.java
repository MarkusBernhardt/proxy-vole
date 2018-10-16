package com.github.markusbernhardt.proxy.search.desktop.win;

import java.net.ProxySelector;
import java.util.Properties;

import com.github.markusbernhardt.proxy.jna.win.WinHttp;
import com.github.markusbernhardt.proxy.jna.win.WinHttpProxyInfo;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Extracts the proxy settings from the windows registry. This will read the
 * windows system proxy settings.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class WinProxySearchStrategy extends CommonWindowsSearchStrategy {

	/*************************************************************************
	 * Constructor
	 ************************************************************************/

	public WinProxySearchStrategy() {
		super();
	}

	/*************************************************************************
	 * getProxySelector
	 *
	 * @see com.github.markusbernhardt.proxy.ProxySearchStrategy#getProxySelector()
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() throws ProxyException {

		Logger.log(getClass(), LogLevel.TRACE, "Detecting Windows proxy settings");

		WinProxyConfig windowsProxyConfig = readWindowsProxyConfig();

		if (windowsProxyConfig.getAccessType() == WinHttp.WINHTTP_ACCESS_TYPE_NO_PROXY) {
			return null;
		} else {
			return createFixedProxySelector(windowsProxyConfig);
		}

	}

	/*************************************************************************
	 * Gets the printable name of the search strategy.
	 *
	 * @return the printable name of the search strategy
	 ************************************************************************/

	@Override
	public String getName() {
		return "windows";
	}

	public WinProxyConfig readWindowsProxyConfig() {

		// Retrieve the Win proxy configuration.
		WinHttpProxyInfo winHttpProxyInfo = new WinHttpProxyInfo();
		boolean result = WinHttp.INSTANCE.WinHttpGetDefaultProxyConfiguration(winHttpProxyInfo);
		if (!result) {
			return null;
		}

		// Create WinProxyConfig instance
		return new WinProxyConfig(
				winHttpProxyInfo.dwAccessType != null ? winHttpProxyInfo.dwAccessType.intValue() : null,
				winHttpProxyInfo.lpszProxy != null ? winHttpProxyInfo.lpszProxy.getValue() : null,
				winHttpProxyInfo.lpszProxyBypass != null ? winHttpProxyInfo.lpszProxyBypass.getValue() : null);
	}

	/*************************************************************************
	 * Parses the proxy settings into an ProxySelector.
	 *
	 * @param winProxyConfig
	 *            the settings to use.
	 * @return a ProxySelector, null if no settings are set.
	 * @throws ProxyException
	 *             on error.
	 ************************************************************************/

	private ProxySelector createFixedProxySelector(WinProxyConfig winProxyConfig) throws ProxyException {
		String proxyString = winProxyConfig.getProxy();
		String bypassList = winProxyConfig.getProxyBypass();
		if (proxyString == null) {
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, "Windows uses manual settings: {0} with bypass list: {1}", proxyString,
		        bypassList);

		Properties p = parseProxyList(proxyString);

		ProtocolDispatchSelector ps = buildProtocolDispatchSelector(p);

		ProxySelector result = setByPassListOnSelector(bypassList, ps);
		return result;
	}
}
