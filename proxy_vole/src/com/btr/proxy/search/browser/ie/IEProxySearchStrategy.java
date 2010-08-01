package com.btr.proxy.search.browser.ie;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Properties;

import com.btr.proxy.search.ProxySearchStrategy;
import com.btr.proxy.search.desktop.win.WinIESettings;
import com.btr.proxy.search.desktop.win.Win32ProxyUtils;
import com.btr.proxy.selector.misc.ProtocolDispatchSelector;
import com.btr.proxy.selector.pac.PacProxySelector;
import com.btr.proxy.selector.pac.UrlPacScriptSource;
import com.btr.proxy.selector.whitelist.ProxyBypassListSelector;
import com.btr.proxy.util.Logger;
import com.btr.proxy.util.ProxyException;
import com.btr.proxy.util.ProxyUtil;
import com.btr.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Extracts the proxy settings for Microsoft Internet Explorer.
 * The settings are read by invoking native Windows API methods.  
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class IEProxySearchStrategy implements ProxySearchStrategy {

	/*************************************************************************
	 * getProxySelector
	 * @see com.btr.proxy.search.ProxySearchStrategy#getProxySelector()
	 ************************************************************************/

	public ProxySelector getProxySelector() throws ProxyException {
		
		Logger.log(getClass(), LogLevel.TRACE, "Detecting IE proxy settings");
		
		WinIESettings ieSettings = readSettings();
		
		ProxySelector result = createPacSelector(ieSettings);
		if (result == null) {
			result = createFixedProxySelector(ieSettings);
		}
		return result;
	}

	/*************************************************************************
	 * Loads the settings from the windows registry.
	 * @return WinIESettings containing all proxy settings.
	 ************************************************************************/
	
	public WinIESettings readSettings() {
		WinIESettings ieSettings = new Win32ProxyUtils().winHttpGetIEProxyConfigForCurrentUser();
		return ieSettings;
	}

	/*************************************************************************
	 * Parses the settings and creates an PAC ProxySelector for it.
	 * @param ieSettings the IE settings to use.
	 * @return a PacProxySelector the selector or null.
	 ************************************************************************/
	
	private PacProxySelector createPacSelector(WinIESettings ieSettings) {
		String pacUrl = null;

		if (ieSettings.isAutoDetect()) {
			Logger.log(getClass(), LogLevel.TRACE, "Autodetecting script URL.");
			// This will take some time.
			pacUrl = new Win32ProxyUtils().winHttpDetectAutoProxyConfigUrl(
					Win32ProxyUtils.WINHTTP_AUTO_DETECT_TYPE_DHCP+
					Win32ProxyUtils.WINHTTP_AUTO_DETECT_TYPE_DNS_A);
		}
		if (pacUrl == null) {
			pacUrl = ieSettings.getAutoConfigUrl();
		}
		if (pacUrl != null && pacUrl.trim().length() > 0) {
			Logger.log(getClass(), LogLevel.TRACE, "IE uses script: "+pacUrl);
			return new PacProxySelector(new UrlPacScriptSource(pacUrl));
		}
		
		return null;
	}

	/*************************************************************************
	 * Parses the proxy settings into an ProxySelector.
	 * @param ieSettings the settings to use.
	 * @return a ProxySelector, null if no settings are set.
	 * @throws ProxyException on error.
	 ************************************************************************/
	
	private ProxySelector createFixedProxySelector(WinIESettings ieSettings) throws ProxyException {
		String proxyString = ieSettings.getProxy();
		String bypassList = ieSettings.getProxyBypass();

		if (proxyString == null) {
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, 
				"IE uses manual settings: {0} with bypass list: {1}", proxyString, bypassList);
		
		Properties p = parseProxyList(proxyString);
		
		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		String proxy = p.getProperty("default");
		if (proxy != null) {
			ps.setFallbackSelector(ProxyUtil.parseProxySettings(proxy));
		}
		
		proxy = p.getProperty("http");
		if (proxy != null) {
			ps.setSelector("http", ProxyUtil.parseProxySettings(proxy));
		}
		
		proxy = p.getProperty("https");
		if (proxy != null) {
			ps.setSelector("https", ProxyUtil.parseProxySettings(proxy));
		}

		proxy = p.getProperty("ftp");
		if (proxy != null) {
			ps.setSelector("ftp", ProxyUtil.parseProxySettings(proxy));
		}

		proxy = p.getProperty("gopher");
		if (proxy != null) {
			ps.setSelector("gopher", ProxyUtil.parseProxySettings(proxy));
		}

		proxy = p.getProperty("socks");
		if (proxy != null) {
			ps.setSelector("socks", ProxyUtil.parseProxySettings(proxy));
		}

		if (bypassList != null && bypassList.trim().length() > 0) {
			bypassList = bypassList.replace(';', ',');
			return new ProxyBypassListSelector(bypassList, ps);
		}
		
		return ps;
	}

	/*************************************************************************
	 * Parses the proxy list and splits it by protocol.
	 * @param proxyString the proxy list string
	 * @return Properties with separated settings.
	 * @throws ProxyException on parse error.
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
				Logger.log(getClass(), LogLevel.ERROR, 
						"Error reading IE settings as properties: {0}", e);
	
				throw new ProxyException(e);
			}
		}
		return p;
	}

}
