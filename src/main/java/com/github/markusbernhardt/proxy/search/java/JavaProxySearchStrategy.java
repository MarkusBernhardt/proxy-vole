package com.github.markusbernhardt.proxy.search.java;

import java.net.ProxySelector;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.fixed.FixedSocksSelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Reads some java system properties and extracts the proxy settings from them.
 * The following variables are read:
 * <ul>
 * <li><i>http.proxyHost</i> (default: none)</li>
 * <li><i>http.proxyPort</i> (default: 80 if http.proxyHost specified)</li>
 * <li><i>http.nonProxyHosts</i> (default: none)</li>
 * </ul>
 * <ul>
 * <li><i>https.proxyHost</i> (default: none)</li>
 * <li><i>https.proxyPort</i> (default: 443 if https.proxyHost specified)</li>
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
 * <p>
 * This is based on information found here: <br>
 * http://download.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
 * </p>
 * If the "http.proxyHost" property is not set then the no proxy selector is
 * setup This property is used as marker to signal that the System settings
 * should be used.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class JavaProxySearchStrategy implements ProxySearchStrategy {

  /*************************************************************************
   * Constructor Will use the default environment variables.
   ************************************************************************/

  public JavaProxySearchStrategy() {
    super();
  }

  /*************************************************************************
   * Loads the proxy settings from environment variables.
   * 
   * @return a configured ProxySelector, null if none is found.
   ************************************************************************/

  @Override
  public ProxySelector getProxySelector() {
    ProtocolDispatchSelector ps = new ProtocolDispatchSelector();

    Logger.log(getClass(), LogLevel.TRACE, "Using settings from Java System Properties");

    setupProxyForProtocol(ps, "http", 80);
    setupProxyForProtocol(ps, "https", 443);
    setupProxyForProtocol(ps, "ftp", 80);
    setupProxyForProtocol(ps, "ftps", 80);
    boolean socksAvailable = setupSocktProxy(ps);

    if (ps.size() == 0 && !socksAvailable) {
      return null;
    }

    return ps;
  }

  /*************************************************************************
   * Gets the printable name of the search strategy.
   * 
   * @return the printable name of the search strategy
   ************************************************************************/

  @Override
  public String getName() {
    return "java";
  }

  /*************************************************************************
   * Parse SOCKS settings
   * 
   * @param ps
   * @throws NumberFormatException
   ************************************************************************/

  private boolean setupSocktProxy(ProtocolDispatchSelector ps) {
    String host = System.getProperty("socksProxyHost");
    if (host == null || host.trim().length() == 0) {
      return false;
    }

    String port = System.getProperty("socksProxyPort", "1080");
    Logger.log(getClass(), LogLevel.TRACE, "Socks proxy {0}:{1} found", host, port);
    ps.setFallbackSelector(new FixedSocksSelector(host, Integer.parseInt(port)));
    return true;
  }

  /*************************************************************************
   * Parse properties for the given protocol.
   * 
   * @param ps
   * @param protocol
   * @throws NumberFormatException
   ************************************************************************/

  private void setupProxyForProtocol(ProtocolDispatchSelector ps, String protocol, int defaultPort) {
    String host = System.getProperty(protocol + ".proxyHost");
    if (host == null || host.trim().length() == 0) {
      return;
    }

    String port = System.getProperty(protocol + ".proxyPort", Integer.toString(defaultPort));
    String whiteList = System.getProperty(protocol + ".nonProxyHosts", "").replace('|', ',');

    if ("https".equalsIgnoreCase(protocol)) { // This is dirty but https has
                                              // no own property for it.
      whiteList = System.getProperty("http.nonProxyHosts", "").replace('|', ',');
    }

    Logger.log(getClass(), LogLevel.TRACE, protocol.toUpperCase() + " proxy {0}:{1} found using whitelist: {2}", host,
        port, whiteList);

    ProxySelector protocolSelector = new FixedProxySelector(host, Integer.parseInt(port));
    if (whiteList.trim().length() > 0) {
      protocolSelector = new ProxyBypassListSelector(whiteList, protocolSelector);
    }

    ps.setSelector(protocol, protocolSelector);
  }

}
