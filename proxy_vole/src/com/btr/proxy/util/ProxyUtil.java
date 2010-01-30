package com.btr.proxy.util;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.btr.proxy.selector.fixed.FixedProxySelector;

/*****************************************************************************
 * Small helper class for some common utility methods.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class ProxyUtil {
	
	public static final int DEFAULT_PROXY_PORT = 80;
	
	private static List<Proxy> noProxyList;
	
	/*************************************************************************
	 * Parse host and port out of a proxy variable.
	 * @param proxyVar the proxy string.
	 * @return a FixedProxySelector using this settings, null on parse error.
	 ************************************************************************/
	
	public static FixedProxySelector parseProxySettings(String proxyVar) {
		if (proxyVar == null || proxyVar.trim().length() == 0) {
			return null;
		}
		int index = proxyVar.lastIndexOf(':');
		if (index != -1) {
			String host = proxyVar.substring(0, index).trim();
			int port = Integer.parseInt(proxyVar.substring(index+1));
			
			// Remove protocol part.
			index = host.indexOf(":/");
			if (index >= 0) {
				while (host.charAt(++index) == '/') {
					// Increment
				}
				host = host.substring(index);
			}
			
			return new FixedProxySelector(host, port);
		}
		return new FixedProxySelector(proxyVar.trim(), DEFAULT_PROXY_PORT);
	}
	
	/*************************************************************************
	 * Gets an unmodifiable proxy list that will have as it's only entry an DIRECT proxy.
	 * @return a list with a DIRECT proxy in it.
	 ************************************************************************/
	
	public static synchronized List<Proxy> noProxyList() {
		if (noProxyList == null) {
			ArrayList<Proxy> list = new ArrayList<Proxy>(1);
			list.add(Proxy.NO_PROXY);
			noProxyList = Collections.unmodifiableList(list);
		}
		return noProxyList;
	}
	
	

}
