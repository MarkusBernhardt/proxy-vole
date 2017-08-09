package com.github.markusbernhardt.proxy.search.desktop.win;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.browser.ie.IELocalByPassFilter;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.github.markusbernhardt.proxy.util.UriFilter;

/**
 * Contains common methods used in search strategies for both Windows and IE.
 */
public abstract class CommonWindowsSearchStrategy implements ProxySearchStrategy {

	/*************************************************************************
	 * Installs the proxy exclude list on the given selector.
	 *
	 * @param bypassList
	 *            the list of urls / hostnames to ignore.
	 * @param ps
	 *            the proxy selector to wrap.
	 * @return a wrapped proxy selector that will handle the bypass list.
	 ************************************************************************/

	protected ProxySelector setByPassListOnSelector(String bypassList, ProtocolDispatchSelector ps) {
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

	protected Properties parseProxyList(String proxyString) throws ProxyException {
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

	protected ProtocolDispatchSelector buildProtocolDispatchSelector(Properties properties) {
		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		addSelectorForProtocol(properties, "http", ps);
		addSelectorForProtocol(properties, "https", ps);
		addSelectorForProtocol(properties, "ftp", ps);
		addSelectorForProtocol(properties, "gopher", ps);
		addSelectorForProtocol(properties, "socks", ps);
		addFallbackSelector(properties, ps);
		return ps;
	}
}
