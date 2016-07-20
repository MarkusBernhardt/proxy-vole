package com.github.markusbernhardt.proxy.search.desktop.osx;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.ProxySelector;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.browser.ie.IELocalByPassFilter;
import com.github.markusbernhardt.proxy.search.wpad.WpadProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.direct.NoProxySelector;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.fixed.FixedSocksSelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.PListParser;
import com.github.markusbernhardt.proxy.util.PListParser.Dict;
import com.github.markusbernhardt.proxy.util.PListParser.XmlParseException;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.github.markusbernhardt.proxy.util.UriFilter;

/*****************************************************************************
 * Loads the OSX system proxy settings from the settings file.
 * <p>
 * All settings are stored in OSX in a special XML file format. These settings
 * file are named plist files and contain nested dictionaries, arrays and
 * values.
 * </p>
 * <p>
 * To parse this file we use a parser that is derived from a plist parser that
 * comes with the xmlwise XML parser package:
 * </p>
 * <p>
 * http://code.google.com/p/xmlwise/
 * </p>
 * <p>
 * I modified that parser to work with the default Java XML parsing library.
 * </p>
 * <p>
 * The plist file is located on OSX at:
 * </p>
 * <p>
 * /Library/Preferences/SystemConfiguration/preferences.plist
 * </p>
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class OsxProxySearchStrategy implements ProxySearchStrategy {

	public static final String OVERRIDE_SETTINGS_FILE = "com.github.markusbernhardt.proxy.osx.settingsFile";
	public static final String OVERRIDE_ACCEPTED_DEVICES = "com.github.markusbernhardt.proxy.osx.acceptedDevices";

	private static final String SETTINGS_FILE = "/Library/Preferences/SystemConfiguration/preferences.plist";

	/*************************************************************************
	 * ProxySelector
	 * 
	 * @see java.net.ProxySelector#ProxySelector()
	 ************************************************************************/

	public OsxProxySearchStrategy() {
		super();
	}

	/*************************************************************************
	 * Loads the proxy settings and initializes a proxy selector for the OSX
	 * proxy settings.
	 * 
	 * @return a configured ProxySelector, null if none is found.
	 * @throws ProxyException
	 *             on file reading error.
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() throws ProxyException {

		Logger.log(getClass(), LogLevel.TRACE, "Detecting OSX proxy settings");

		try {
			List<String> acceptedInterfaces = getNetworkInterfaces();

			Dict settings = PListParser.load(getSettingsFile());
			Object currentSet = settings.getAtPath("/CurrentSet");
			if (currentSet == null) {
				throw new ProxyException("CurrentSet not defined");
			}

			Dict networkSet = (Dict) settings.getAtPath(String.valueOf(currentSet));

			// TODO 30.03.2015 bros Test for IP6 compatibility
			List<?> serviceOrder = (List<?>) networkSet.getAtPath("/Network/Global/IPv4/ServiceOrder");
			if (serviceOrder == null || serviceOrder.size() == 0) {
				throw new ProxyException("ServiceOrder not defined");
			}

			// Look at the Services in priority order and pick the first one
			// that was
			// also accepted above
			Dict proxySettings = null;
			for (int i = 0; i < serviceOrder.size() && proxySettings == null; i++) {
				Object candidateService = serviceOrder.get(i);
				Object networkService = networkSet.getAtPath("/Network/Service/" + candidateService + "/__LINK__");
				if (networkService == null) {
					throw new ProxyException("NetworkService not defined.");
				}
				Dict selectedServiceSettings = (Dict) settings.getAtPath("" + networkService);
				String interfaceName = (String) selectedServiceSettings.getAtPath("/Interface/DeviceName");
				if (acceptedInterfaces.contains(interfaceName)) {
					Logger.log(getClass(), LogLevel.TRACE, "Looking up proxies for device " + interfaceName);
					proxySettings = (Dict) selectedServiceSettings.getAtPath("/Proxies");
				}
			}
			if (proxySettings == null) {
				return NoProxySelector.getInstance();
			}

			return buildSelector(proxySettings);
		} catch (XmlParseException e) {
			throw new ProxyException(e);
		} catch (IOException e) {
			throw new ProxyException(e);
		}
	}

	/*************************************************************************
	 * Gets the printable name of the search strategy.
	 * 
	 * @return the printable name of the search strategy
	 ************************************************************************/

	@Override
	public String getName() {
		return "osx";
	}

	/*************************************************************************
	 * Build a selector from the given settings.
	 * 
	 * @param proxySettings
	 *            to parse
	 * @return the configured selector
	 * @throws ProxyException
	 *             on error
	 ************************************************************************/

	private ProxySelector buildSelector(Dict proxySettings) throws ProxyException {
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

		if (result != null) {
			result = installExceptionList(proxySettings, result);
			result = installSimpleHostFilter(proxySettings, result);
		}
		return result;
	}

	/*************************************************************************
	 * Create a list of Ethernet interfaces that are connected
	 * 
	 * @return a list of available interface names
	 * @throws SocketException
	 ************************************************************************/

	private List<String> getNetworkInterfaces() throws SocketException {
		String override = System.getProperty(OVERRIDE_ACCEPTED_DEVICES);
		if (override != null && override.length() > 0) {
			return Arrays.asList(override.split(";"));
		}

		List<String> acceptedInterfaces = new ArrayList<String>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface ni = interfaces.nextElement();
			if (isInterfaceAllowed(ni)) {
				acceptedInterfaces.add(ni.getName());
			}
		}
		return acceptedInterfaces;
	}

	/*************************************************************************
	 * Check if a given network interface is interesting for us.
	 * 
	 * @param ni
	 *            the interface to check
	 * @return true if accepted else false.
	 * @throws SocketException
	 *             on error.
	 ************************************************************************/

	private boolean isInterfaceAllowed(NetworkInterface ni) throws SocketException {
		return !ni.isLoopback() && !ni.isPointToPoint() && // Not sure if we
		                                                   // should filter the
		                                                   // point to point
		                                                   // interfaces?
		        !ni.isVirtual() && ni.isUp();
	}

	/*************************************************************************
	 * Gets the settings file to parse the settings from.
	 * 
	 * @return the settings file.
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
	 * Install a filter to ignore simple host names without domain name.
	 * 
	 * @param proxySettings
	 *            the dictionary containing all settings
	 * @param result
	 *            the proxy selector that needs to be adapted.
	 * @return a wrapped proxy selector that will ignore simple names.
	 ************************************************************************/

	private ProxySelector installSimpleHostFilter(Dict proxySettings, ProxySelector result) {
		if (isActive(proxySettings.get("ExcludeSimpleHostnames"))) {
			List<UriFilter> localBypassFilter = new ArrayList<UriFilter>();
			localBypassFilter.add(new IELocalByPassFilter());
			result = new ProxyBypassListSelector(localBypassFilter, result);
		}
		return result;
	}

	/*************************************************************************
	 * Install a host name base filter to handle the proxy exclude list.
	 * 
	 * @param proxySettings
	 *            the dictionary containing all settings
	 * @param result
	 *            the proxy selector that needs to be adapted.
	 * @return a wrapped proxy selector that will handle the exclude list.
	 ************************************************************************/

	private ProxySelector installExceptionList(Dict proxySettings, ProxySelector result) {
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
	 * 
	 * @param proxyExceptions
	 *            list of elements.
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
	 * Invoke WPAD proxy detection if configured.
	 * 
	 * @param proxySettings
	 *            the settings to analyse.
	 * @param result
	 *            the current proxy selector.
	 * @return a WPAD proxy selector or the passed in proxy selector.
	 * @throws ProxyException
	 *             on automatic detection errors.
	 ************************************************************************/

	private ProxySelector autodetectProxyIfAvailable(Dict proxySettings, ProxySelector result) throws ProxyException {
		if (isActive(proxySettings.get("ProxyAutoDiscoveryEnable"))) {
			ProxySelector wp = new WpadProxySearchStrategy().getProxySelector();
			if (wp != null) {
				result = wp;
			}
		}
		return result;
	}

	/*************************************************************************
	 * Use a PAC based proxy selector if configured.
	 * 
	 * @param proxySettings
	 *            the settings to analyse.
	 * @param result
	 *            the current proxy selector.
	 * @return a PAC proxy selector or the passed in proxy selector.
	 ************************************************************************/

	private ProxySelector installPacProxyIfAvailable(Dict proxySettings, ProxySelector result) {
		if (isActive(proxySettings.get("ProxyAutoConfigEnable"))) {
			String url = (String) proxySettings.get("ProxyAutoConfigURLString");
			result = ProxyUtil.buildPacSelectorForUrl(url);
		}
		return result;
	}

	/*************************************************************************
	 * Build a socks proxy and set it for the socks protocol.
	 * 
	 * @param proxySettings
	 *            to read the config values from.
	 * @param ps
	 *            the ProtocolDispatchSelector to install the new proxy on.
	 ************************************************************************/

	private void installSocksProxy(Dict proxySettings, ProtocolDispatchSelector ps) {
		if (isActive(proxySettings.get("SOCKSEnable"))) {
			String proxyHost = (String) proxySettings.get("SOCKSProxy");
			int proxyPort = (Integer) proxySettings.get("SOCKSPort");
			ps.setSelector("socks", new FixedSocksSelector(proxyHost, proxyPort));
			Logger.log(getClass(), LogLevel.TRACE, "OSX socks proxy is {0}:{1}", proxyHost, proxyPort);
		}
	}

	/*************************************************************************
	 * Installs a proxy selector for the given protocoll on the
	 * ProtocolDispatchSelector
	 * 
	 * @param proxySettings
	 *            to read the config for the procotol from.
	 * @param ps
	 *            the ProtocolDispatchSelector to install the new selector on.
	 * @param protocol
	 *            to use.
	 ************************************************************************/

	private void installSelectorForProtocol(Dict proxySettings, ProtocolDispatchSelector ps, String protocol) {
		String prefix = protocol.trim();
		if (isActive(proxySettings.get(prefix + "Enable"))) {
			String proxyHost = (String) proxySettings.get(prefix + "Proxy");
			int proxyPort = (Integer) proxySettings.get(prefix + "Port");
			FixedProxySelector fp = new FixedProxySelector(proxyHost, proxyPort);
			ps.setSelector(protocol.toLowerCase(), fp);
			Logger.log(getClass(), LogLevel.TRACE, "OSX uses for {0} the proxy {1}:{2}", protocol, proxyHost,
			        proxyPort);
		}
	}

	/*************************************************************************
	 * Checks if the given value is set to "on".
	 * 
	 * @param value
	 *            the value to test.
	 * @return true if it is set else false.
	 ************************************************************************/

	private boolean isActive(Object value) {
		return Integer.valueOf(1).equals(value);
	}

}
