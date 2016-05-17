package com.github.markusbernhardt.proxy.search.browser.ie;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.jna.win.WinHttp;
import com.github.markusbernhardt.proxy.jna.win.WinHttpCurrentUserIEProxyConfig;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.github.markusbernhardt.proxy.util.UriFilter;
import com.sun.jna.platform.win32.WTypes.LPWSTR;
import com.sun.jna.platform.win32.WinDef.DWORD;

/*****************************************************************************
 * Extracts the proxy settings for Microsoft Internet Explorer. The settings are
 * read by invoking native Windows API methods.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class IEProxySearchStrategy implements ProxySearchStrategy {

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

		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		addSelectorForProtocol(p, "http", ps);
		addSelectorForProtocol(p, "https", ps);
		addSelectorForProtocol(p, "ftp", ps);
		addSelectorForProtocol(p, "gopher", ps);
		addSelectorForProtocol(p, "socks", ps);
		addFallbackSelector(p, ps);

		ProxySelector result = setByPassListOnSelector(bypassList, ps);
		return result;
	}

	/*************************************************************************
	 * Installs the proxy exclude list on the given selector.
	 * 
	 * @param bypassList
	 *            the list of urls / hostnames to ignore.
	 * @param ps
	 *            the proxy selector to wrap.
	 * @return a wrapped proxy selector that will handle the bypass list.
	 ************************************************************************/

	private ProxySelector setByPassListOnSelector(String bypassList, ProtocolDispatchSelector ps) {
		if (bypassList != null && bypassList.trim().length() > 0) {
			ProxyBypassListSelector result;
			if ("<local>".equals(bypassList.trim())) {
				result = buildLocalBypassSelector(ps);
			} else {
				bypassList = bypassList.replace(';', ',');
				result = new ProxyBypassListSelector(bypassList, ps);
			}
			return result;
		}
		return ps;
	}

	/*************************************************************************
	 * Wraps the given selector to handle "local" addresses
	 * 
	 * @param ps
	 *            the proxy selector to wrap.
	 * @return a wrapped proxy selector that will handle the local addresses.
	 ************************************************************************/

	private ProxyBypassListSelector buildLocalBypassSelector(ProtocolDispatchSelector ps) {
		List<UriFilter> localBypassFilter = new ArrayList<UriFilter>();
		localBypassFilter.add(new IELocalByPassFilter());
		return new ProxyBypassListSelector(localBypassFilter, ps);
	}

	/*************************************************************************
	 * Installs a fallback selector that is used whenever no protocol specific
	 * selector is defined.
	 * 
	 * @param settings
	 *            to take the proxy settings from.
	 * @param ps
	 *            to install the created selector on.
	 ************************************************************************/

	private void addFallbackSelector(Properties settings, ProtocolDispatchSelector ps) {
		String proxy = settings.getProperty("default");
		if (proxy != null) {
			ps.setFallbackSelector(ProxyUtil.parseProxySettings(proxy));
		}
	}

	/*************************************************************************
	 * Creates a selector for a given protocol. The proxy will be taken from the
	 * settings and installed on the dispatch selector.
	 * 
	 * @param settings
	 *            to take the proxy settings from.
	 * @param protocol
	 *            to create a selector for.
	 * @param ps
	 *            to install the created selector on.
	 ************************************************************************/

	private void addSelectorForProtocol(Properties settings, String protocol, ProtocolDispatchSelector ps) {
		String proxy = settings.getProperty(protocol);
		if (proxy != null) {
			FixedProxySelector protocolSelector = ProxyUtil.parseProxySettings(proxy);
			ps.setSelector(protocol, protocolSelector);
		}
	}

	/*************************************************************************
	 * Parses the proxy list and splits it by protocol.
	 * 
	 * @param proxyString
	 *            the proxy list string
	 * @return Properties with separated settings.
	 * @throws ProxyException
	 *             on parse error.
	 ************************************************************************/

	private Properties parseProxyList(String proxyString) throws ProxyException {
		Properties p = new Properties();
		if (proxyString.indexOf('=') == -1) {
			p.setProperty("default", proxyString);
		} else {
			try {
				proxyString = proxyString.replace(';', '\n');
				p.load(new ByteArrayInputStream(proxyString.getBytes("ISO-8859-1")));
			} catch (IOException e) {
				Logger.log(getClass(), LogLevel.ERROR, "Error reading IE settings as properties: {0}", e);

				throw new ProxyException(e);
			}
		}
		return p;
	}

}
