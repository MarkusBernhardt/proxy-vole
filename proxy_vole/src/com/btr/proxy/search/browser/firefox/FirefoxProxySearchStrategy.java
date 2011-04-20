package com.btr.proxy.search.browser.firefox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Properties;

import com.btr.proxy.search.ProxySearchStrategy;
import com.btr.proxy.search.desktop.DesktopProxySearchStrategy;
import com.btr.proxy.search.wpad.WpadProxySearchStrategy;
import com.btr.proxy.selector.direct.NoProxySelector;
import com.btr.proxy.selector.fixed.FixedProxySelector;
import com.btr.proxy.selector.misc.ProtocolDispatchSelector;
import com.btr.proxy.selector.pac.PacProxySelector;
import com.btr.proxy.selector.pac.UrlPacScriptSource;
import com.btr.proxy.selector.whitelist.ProxyBypassListSelector;
import com.btr.proxy.util.Logger;
import com.btr.proxy.util.PlatformUtil;
import com.btr.proxy.util.ProxyException;
import com.btr.proxy.util.Logger.LogLevel;
import com.btr.proxy.util.PlatformUtil.Platform;

/*****************************************************************************
 * Loads the Firefox3 proxy settings from the users Firefox3 settings.
 * This will load the file <i>prefs.js</i> that is located in the 
 * <p>
 * <i>.mozilla/firefox/(profile)/</i> folder. 
 * </p>
 * 
 * See <a href="https://developer.mozilla.org/En/Mozilla_Networking_Preferences">Mozilla_Networking_Preferences</a> 
 * for an explanation of the proxy settings.
 * <p>
 * The following settings are extracted from 
 * this file: 
 * </p> 
 * Some generic settings:<br/>
 * <ul>
 * <li><i>network.proxy.type</i> -> n/a = use system settings, 0 = direct, 1 = Fixed proxy settings, 2 = proxy script (PAC), 3 = also direct , 4 = auto detect (WPAD)</li>
 * <li><i>network.proxy.share_proxy_settings</i> -> true = use same proxy for all protocols</li>
 * <li><i>network.proxy.no_proxies_on</i> -> a comma separated ignore list. </li>
 * <li><i>network.proxy.autoconfig_url</i> -> a URL to an proxy configuration script</li>
 * </ul>
 * Host names and ports per protocol are stored in the following settings:
 * <ul>
 * <li><i>network.proxy.http</i></li>
 * <li><i>network.proxy.http_port</i></li>
 * <li><i>network.proxy.ssl</i></li>
 * <li><i>network.proxy.ssl_port</i></li>
 * <li><i>network.proxy.ftp</i></li>
 * <li><i>network.proxy.ftp_port</i></li>
 * <li><i>network.proxy.gopher</i></li>
 * <li><i>network.proxy.gopher_port</i></li>
 * <li><i>network.proxy.socks</i></li>
 * <li><i>network.proxy.socks_port</i></li>
 * <li><i>network.proxy.socks_version</i> -> 4 or 5</li>
 * </u>
 * <p>
 * Note that if there are more than one profile the first profile found will be used.
 * </p>
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class FirefoxProxySearchStrategy implements ProxySearchStrategy {
	
	private FirefoxProfileSource profileScanner;
	private FirefoxSettingParser settingsParser;

	/*************************************************************************
	 * ProxySelector
	 * @see java.net.ProxySelector#ProxySelector()
	 ************************************************************************/
	
	public FirefoxProxySearchStrategy() {
		super();
		if (PlatformUtil.getCurrentPlattform() == Platform.WIN) {
			this.profileScanner = new WinFirefoxProfileSource();
		} else {
			this.profileScanner = new LinuxFirefoxProfileSource();
		}
		this.settingsParser = new FirefoxSettingParser();
	}
	
	/*************************************************************************
	 * Loads the proxy settings and initializes a proxy selector for the firefox
	 * proxy settings.
	 * @return a configured ProxySelector, null if none is found.
	 * @throws ProxyException on file reading error. 
	 ************************************************************************/

	public ProxySelector getProxySelector() throws ProxyException {
		Logger.log(getClass(), LogLevel.TRACE, "Detecting Firefox settings.");

		Properties settings = readSettings();
		
		ProxySelector result = null; 
		int type = Integer.parseInt(settings.getProperty("network.proxy.type", "-1"));
		switch (type) {
			case -1: // Use system settings
				Logger.log(getClass(), LogLevel.TRACE, "Firefox uses system settings");
				result = new DesktopProxySearchStrategy().getProxySelector();
				break;
			case 0: // Use no proxy 
				Logger.log(getClass(), LogLevel.TRACE, "Firefox uses no proxy");
				result = NoProxySelector.getInstance();
				break;
			case 1: // Fixed settings
				Logger.log(getClass(), LogLevel.TRACE, "Firefox uses manual settings");
				result = setupFixedProxySelector(settings);
				break;
			case 2: // PAC Script
				String pacScriptUrl = settings.getProperty("network.proxy.autoconfig_url", "");
				Logger.log(getClass(), LogLevel.TRACE, "Firefox uses script (PAC) {0}", pacScriptUrl);
				result = new PacProxySelector(new UrlPacScriptSource(pacScriptUrl));
				break;
			case 3: // Backward compatibility to netscape.
				Logger.log(getClass(), LogLevel.TRACE, "Netscape compability mode -> uses no proxy");
				result = NoProxySelector.getInstance();
				break;
			case 4: // WPAD auto config
				Logger.log(getClass(), LogLevel.TRACE, "Firefox uses automatic detection (WPAD)");
				result = new WpadProxySearchStrategy().getProxySelector();
				break;
			default:
				break;
		}

		// Wrap in white list filter.
		String noProxyList = settings.getProperty("network.proxy.no_proxies_on", null);
		if (result != null && noProxyList != null && noProxyList.trim().length() > 0) {
			Logger.log(getClass(), LogLevel.TRACE, "Firefox uses proxy bypass list for: {0}", noProxyList);
			result = new ProxyBypassListSelector(noProxyList, result);
		}
		
		return result;
	}

	/*************************************************************************
	 * Reads the settings file and stores all settings in a Properties map.
	 * @return the parsed settings.
	 * @throws ProxyException on read error.
	 ************************************************************************/
	
	public Properties readSettings() throws ProxyException {
		try {
			Properties settings = this.settingsParser.parseSettings(this.profileScanner);
			return settings;
		} catch (IOException e) {
			Logger.log(getClass(), LogLevel.ERROR, "Error parsing settings", e);
			throw new ProxyException(e);
		}
	}

	/*************************************************************************
	 * Parse the fixed proxy settings and build an ProxySelector for this a 
	 * chained configuration.
	 * @param settings the proxy settings to evaluate.
	 ************************************************************************/
	
	private ProxySelector setupFixedProxySelector(Properties settings) {
		boolean shared = "true".equals(settings.getProperty("network.proxy.share_proxy_settings", "false").toLowerCase());

		// HTTP Proxy
		String proxyHost = settings.getProperty("network.proxy.http", null);
		int proxyPort = Integer.parseInt(settings.getProperty("network.proxy.http_port", "0"));
		if (proxyHost == null) {
			return null;
		}
		FixedProxySelector httpProxy = new FixedProxySelector(proxyHost, proxyPort);
		Logger.log(getClass(), LogLevel.TRACE, "Firefox http proxy is {0}:{1}", proxyHost, proxyPort);
		if (shared) {
			return httpProxy;
		}
		
		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		ps.setSelector("http", httpProxy);
		
		// All other proxy server
		proxyHost = settings.getProperty("network.proxy.ftp", null);
		proxyPort = Integer.parseInt(settings.getProperty("network.proxy.ftp_port", "0"));
		if (proxyHost != null && proxyPort != 0) {
			Logger.log(getClass(), LogLevel.TRACE, "Firefox ftp proxy is {0}:{1}", proxyHost, proxyPort);
			ps.setSelector("ftp", new FixedProxySelector(proxyHost, proxyPort));
		}
		
		proxyHost = settings.getProperty("network.proxy.ssl", null);
		proxyPort = Integer.parseInt(settings.getProperty("network.proxy.ssl_port", "0"));
		if (proxyHost != null && proxyPort != 0) {
			Logger.log(getClass(), LogLevel.TRACE, "Firefox secure proxy is {0}:{1}", proxyHost, proxyPort);
			ps.setSelector("https", new FixedProxySelector(proxyHost, proxyPort));
			ps.setSelector("sftp", new FixedProxySelector(proxyHost, proxyPort));
		}

        proxyHost = settings.getProperty("network.proxy.socks", null);
        proxyPort = Integer.parseInt(settings.getProperty("network.proxy.socks_port", "0"));
        if (proxyHost != null && proxyPort != 0) {
                Logger.log(getClass(), LogLevel.TRACE, "Firefox socks proxy is {0}:{1}", proxyHost, proxyPort);
                Proxy socksProxy =  new Proxy(Proxy.Type.SOCKS, 
        				InetSocketAddress.createUnresolved(proxyHost, proxyPort));
                ps.setSelector("socks", new FixedProxySelector(socksProxy));
        }
		
		return ps;
	}
	

}
