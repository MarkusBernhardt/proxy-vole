package com.btr.proxy.search.java;

import java.net.ProxySelector;

import com.btr.proxy.search.ProxySearchStrategy;
import com.btr.proxy.selector.fixed.FixedProxySelector;
import com.btr.proxy.selector.fixed.FixedSocksSelector;
import com.btr.proxy.selector.misc.ProtocolDispatchSelector;
import com.btr.proxy.selector.whitelist.ProxyBypassListSelector;
import com.btr.proxy.util.Logger;
import com.btr.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Reads some java system properties and extracts the proxy settings from them.
 * The following variables are read:
 * <ul>
 * <li><i>http.proxyHost</i> (default: none)</li>
 * <li><i>http.proxyPort</i> (default: 80 if http.proxyHost specified)</li>
 * <li><i>http.nonProxyHosts</i> (default: none)</li>
 * </ul>
 * <ul>
 * <li><i>ftp.proxyHost</i> (default: none)</li>
 * <li><i>ftp.proxyPort</i> (default: 80 if ftp.proxyHost specified)</li>
 * <li><i>ftp.nonProxyHosts</i> (default: none)</li> 
 * </ul>
 * <ul>
 * <li><i>socksProxyHost</i></li>
 * <li><i>socksProxyPort</i> (default: 1080)</li>
 * </ul>
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class JavaProxySearchStrategy implements ProxySearchStrategy {
	
	/*************************************************************************
	 * Constructor
	 * Will use the default environment variables.
	 ************************************************************************/
	
	public JavaProxySearchStrategy() {
		super();
	}
	
	/*************************************************************************
	 * Loads the proxy settings from environment variables.
	 * @return a configured ProxySelector, null if none is found.
	 ************************************************************************/

	public ProxySelector getProxySelector() {
		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		
		Logger.log(getClass(), LogLevel.TRACE, "Using settings from Java System Properties");
		
		// Parse http properties
		String host = System.getProperty("http.proxyHost");
		String port = System.getProperty("http.proxyPort", "80");
		String whiteList = System.getProperty("http.nonProxyHosts", "").replace('|', ',');
		
		if (host == null || host.trim().length() == 0) {
			return null;
		}

		Logger.log(getClass(), LogLevel.TRACE, "HTTP(S) proxy {0}:{1} found using whitelist: {2}", host, port, whiteList);
		
		if (whiteList.trim().length() > 0) {
			ps.setSelector("http", new ProxyBypassListSelector(whiteList, 
					new FixedProxySelector(host, Integer.parseInt(port))));
			ps.setSelector("https", new ProxyBypassListSelector(whiteList, 
					new FixedProxySelector(host, Integer.parseInt(port))));
		} else {
			ps.setSelector("http", new FixedProxySelector(host, Integer.parseInt(port)));
			ps.setSelector("https", new FixedProxySelector(host, Integer.parseInt(port)));
		}

		// Parse ftp properties
		host = System.getProperty("ftp.proxyHost");
		port = System.getProperty("ftp.proxyPort", "80");
		whiteList = System.getProperty("ftp.nonProxyHosts", "").replace('|', ',');
		if (host != null && host.trim().length() > 0) {
			Logger.log(getClass(), LogLevel.TRACE, "Ftp proxy {0}:{1} found using whitelist: {2}", host, port, whiteList);
			if (whiteList.trim().length() > 0) {
				ps.setSelector("ftp", new ProxyBypassListSelector(whiteList, 
						new FixedProxySelector(host, Integer.parseInt(port))));
				ps.setSelector("ftps", new ProxyBypassListSelector(whiteList, 
						new FixedProxySelector(host, Integer.parseInt(port))));
			} else {
				ps.setSelector("ftp", new FixedProxySelector(host, Integer.parseInt(port)));
				ps.setSelector("ftps", new FixedProxySelector(host, Integer.parseInt(port)));
			}
		}

		// Parse SOCKS settings
		host = System.getProperty("socksProxyHost");
		port = System.getProperty("socksProxyPort", "1080");
		if (host != null && host.trim().length() > 0) {
			Logger.log(getClass(), LogLevel.TRACE, "Socks proxy {0}:{1} found", host, port);
			ps.setSelector("socks", new FixedSocksSelector(host, Integer.parseInt(port)));
		}
		
		return ps;
	}

}
