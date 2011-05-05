package com.btr.proxy.search.desktop.osx;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;
import com.btr.proxy.search.ProxySearchStrategy;
import com.btr.proxy.search.browser.ie.IELocalByPassFilter;
import com.btr.proxy.search.wpad.WpadProxySearchStrategy;
import com.btr.proxy.selector.fixed.FixedProxySelector;
import com.btr.proxy.selector.fixed.FixedSocksSelector;
import com.btr.proxy.selector.misc.ProtocolDispatchSelector;
import com.btr.proxy.selector.pac.PacProxySelector;
import com.btr.proxy.selector.pac.PacScriptSource;
import com.btr.proxy.selector.pac.UrlPacScriptSource;
import com.btr.proxy.selector.whitelist.ProxyBypassListSelector;
import com.btr.proxy.util.Logger;
import com.btr.proxy.util.PListParser;
import com.btr.proxy.util.PListParser.Dict;
import com.btr.proxy.util.UriFilter;
import com.btr.proxy.util.PListParser.XmlParseException;
import com.btr.proxy.util.ProxyException;
import com.btr.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Loads the OSX system proxy settings from the settings file.
 * <p>
 * All settings are stored in OSX in a special XML file format.
 * These settings file are named plist files and contain nested dictionaries, arrays and values.
 * </p><p>
 * To parse this file we use a parser that is derived from a plist parser that 
 * comes with the xmlwise XML parser package:
 * </p><p>
 * http://code.google.com/p/xmlwise/
 * </p><p>
 * I modified that parser to work with the default Java XML parsing library.
 *  </p><p>
 * The plist file is located on OSX at:
 * </p><p>
 * /Library/Preferences/SystemConfiguration/preferences.plist
 * </p> 
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2011
 ****************************************************************************/

public class OsxProxySearchStrategy implements ProxySearchStrategy {
	
    public static final String OVERRIDE_SETTINGS_FILE = "com.btr.proxy.osx.settingsFile";
	
	private static final String SETTINGS_FILE = "/Library/Preferences/SystemConfiguration/preferences.plist"; 
		
	/*************************************************************************
	 * ProxySelector
	 * @see java.net.ProxySelector#ProxySelector()
	 ************************************************************************/
	
	public OsxProxySearchStrategy() {
		super();
	}
	
	/*************************************************************************
	 * Loads the proxy settings and initializes a proxy selector for the OSX
	 * proxy settings.
	 * @return a configured ProxySelector, null if none is found.
	 * @throws ProxyException on file reading error. 
	 ************************************************************************/

	public ProxySelector getProxySelector() throws ProxyException {
		
		Logger.log(getClass(), LogLevel.TRACE, "Detecting OSX proxy settings");

		try {
			Dict settings = PListParser.load(getSettingsFile());
			Object currentSet = settings.getAtPath("/CurrentSet");
			if (currentSet == null) {
				throw new ProxyException("CurrentSet not defined");
			}
			
			Dict networkSet = (Dict) settings.getAtPath(String.valueOf(currentSet));
			List<?> serviceOrder = (List<?>) networkSet.getAtPath("/Network/Global/IPv4/ServiceOrder");
			if (serviceOrder == null || serviceOrder.size() == 0) {
				throw new ProxyException("ServiceOrder not defined");
			}
			Object firstService = serviceOrder.get(0);
			Object networkService = networkSet.getAtPath("/Network/Service/"+firstService+"/__LINK__");
			if (networkService == null ) {
				throw new ProxyException("NetworkService not defined.");
			}
			Dict selectedServiceSettings = (Dict) settings.getAtPath(""+networkService);
			Dict proxySettings = (Dict) selectedServiceSettings.getAtPath("/Proxies");

			ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
			installSelectorForProtocol(proxySettings, ps, "HTTP");
			installSelectorForProtocol(proxySettings, ps, "HTTPS");
			installSelectorForProtocol(proxySettings, ps, "FTP");
			installSelectorForProtocol(proxySettings, ps, "Gopher");
			installSelectorForProtocol(proxySettings, ps, "RTSP");
			installSocksProxy(proxySettings, ps);

			ProxySelector result = ps; 
			result = installPacProxyIfAvailable(proxySettings, result);
			result = autodetectProxyIfAvailable(proxySettings, result);
			
			result = installExceptionList(proxySettings, result);		
			result = installSimpleHostFilter(proxySettings, result);
			return result;
		} catch (XmlParseException e) {
			throw new ProxyException(e);
		} catch (IOException e) {
			throw new ProxyException(e);
		}
	}

	/*************************************************************************
	 * @return
	 ************************************************************************/
	
	private File getSettingsFile() {
		File result = new File(SETTINGS_FILE); 
		String overrideFile = System.getProperty(OVERRIDE_SETTINGS_FILE);
		if (overrideFile != null) { 
			return new File(overrideFile);
		}
		return result;
	}

	/*************************************************************************
	 * @param proxySettings
	 * @param result
	 * @return
	 ************************************************************************/
	
	private ProxySelector installSimpleHostFilter(
			Dict proxySettings, ProxySelector result) {
		if (isActive(proxySettings.get("ExcludeSimpleHostnames"))) {
			List<UriFilter> localBypassFilter = new ArrayList<UriFilter>();
			localBypassFilter.add(new IELocalByPassFilter());
			result = new ProxyBypassListSelector(localBypassFilter, result);
		}
		return result;
	}

	/*************************************************************************
	 * @param proxySettings
	 * @param result
	 * @return
	 ************************************************************************/
	
	private ProxySelector installExceptionList(
			Dict proxySettings, ProxySelector result) {
		List<?> proxyExceptions = (List<?>) proxySettings.get("ExceptionsList");
		if (proxyExceptions != null && proxyExceptions.size() > 0) {
			Logger.log(getClass(), LogLevel.TRACE, "OSX uses proxy bypass list: {0}", proxyExceptions);
			String noProxyList = toCommaSeparatedString(proxyExceptions);
			result = new ProxyBypassListSelector(noProxyList, result);
		}
		return result;
	}

	/*************************************************************************
	 * Convert a list to a comma separated list.
	 * @param proxyExceptions list of elements.
	 * @return a comma separated string of the list's content.
	 ************************************************************************/
	
	private String toCommaSeparatedString(List<?> proxyExceptions) {
		StringBuilder result = new StringBuilder();
		for (Object object : proxyExceptions) {
			if (result.length() > 0) {
				result.append(",");
			}
			result.append(object);
		}
		return result.toString();
	}

	/*************************************************************************
	 * @param proxySettings
	 * @param result
	 * @return
	 * @throws ProxyException
	 ************************************************************************/
	
	private ProxySelector autodetectProxyIfAvailable(
			Dict proxySettings, ProxySelector result)
			throws ProxyException {
		if (isActive(proxySettings.get("ProxyAutoDiscoveryEnable"))) {
			ProxySelector wp = new WpadProxySearchStrategy().getProxySelector();
			if (wp != null) {
				result = wp;
			}
		}
		return result;
	}

	/*************************************************************************
	 * @param proxySettings
	 * @param result
	 * @return
	 ************************************************************************/
	
	private ProxySelector installPacProxyIfAvailable(Dict proxySettings,
			ProxySelector result) {
		if (isActive(proxySettings.get("ProxyAutoConfigEnable"))) {
			String url = (String) proxySettings.get("ProxyAutoConfigURLString");
			PacScriptSource pacSource = new UrlPacScriptSource(url);
			result = new PacProxySelector(pacSource);
		}
		return result;
	}

	/*************************************************************************
	 * Build a socks proxy and set it for the socks protocol.
	 * @param proxySettings to read the config values from.
	 * @param ps the ProtocolDispatchSelector to install the new proxy on.
	 ************************************************************************/
	
	private void installSocksProxy(Dict proxySettings,
			ProtocolDispatchSelector ps) {
		if (isActive(proxySettings.get("SOCKSEnable"))) {
			String proxyHost = (String) proxySettings.get("SOCKSProxy");
			int proxyPort = (Integer) proxySettings.get("SOCKSPort");
		    ps.setSelector("socks", new FixedSocksSelector(proxyHost, proxyPort));
			Logger.log(getClass(), LogLevel.TRACE, "OSX socks proxy is {0}:{1}", proxyHost, proxyPort);
		}
	}

	/*************************************************************************
	 * Installs a proxy selector for the given protocoll on the ProtocolDispatchSelector
	 * @param proxySettings to read the config for the procotol from.
	 * @param ps the ProtocolDispatchSelector to install the new selector on.
	 * @param protocol to use.
	 ************************************************************************/
	
	private void installSelectorForProtocol(Dict proxySettings,
			ProtocolDispatchSelector ps, String protocol) {
		String prefix = protocol.trim(); 
		if (isActive(proxySettings.get(prefix+"Enable"))) {
			String proxyHost = (String) proxySettings.get(prefix+"Proxy");
			int proxyPort = (Integer) proxySettings.get(prefix+"Port");
			FixedProxySelector fp = new FixedProxySelector(proxyHost, proxyPort);
			ps.setSelector(protocol.toLowerCase(), fp);
			Logger.log(getClass(), LogLevel.TRACE, "OSX uses for {0} the proxy {1}:{2}", protocol, proxyHost, proxyPort);
		}
	}

	/*************************************************************************
	 * Checks if the given value is set to "on". 
	 * @param value the value to test.
	 * @return true if it is set else false.
	 ************************************************************************/
	
	private boolean isActive(Object value) {
		return Integer.valueOf(1).equals(value);
	}

}
