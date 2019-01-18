package com.github.markusbernhardt.proxy.selector.pac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyUtil;

/*****************************************************************************
 * ProxySelector that will use a PAC script to find an proxy for a given URI.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/
public class PacProxySelector extends ProxySelector {

  // private static final String PAC_PROXY = "PROXY";
  private static final String PAC_SOCKS = "SOCKS";
  private static final String PAC_DIRECT = "DIRECT";

  private PacScriptParser pacScriptParser;

  private static volatile boolean enabled = true;

  /*************************************************************************
   * Constructor
   * 
   * @param pacSource
   *          the source for the PAC file.
   ************************************************************************/

  public PacProxySelector(PacScriptSource pacSource) {
    super();
    selectEngine(pacSource);
  }

  /*************************************************************************
   * Can be used to enable / disable the proxy selector. If disabled it will
   * return DIRECT for all urls.
   * 
   * @param enable
   *          the new status to set.
   ************************************************************************/

  public static void setEnabled(boolean enable) {
    enabled = enable;
  }

  /*************************************************************************
   * Checks if the selector is currently enabled.
   * 
   * @return true if enabled else false.
   ************************************************************************/

  public static boolean isEnabled() {
    return enabled;
  }

  /*************************************************************************
   * Selects one of the available PAC parser engines.
   * 
   * @param pacSource
   *          to use as input.
   ************************************************************************/

  private void selectEngine(PacScriptSource pacSource) {
    try {
      Logger.log(getClass(), LogLevel.INFO, "Using javax.script JavaScript engine.");
      pacScriptParser = new JavaxPacScriptParser(pacSource);
    } catch (Exception e) {
      Logger.log(getClass(), LogLevel.ERROR, "PAC parser error.", e);
    }
  }

  /*************************************************************************
   * connectFailed
   * 
   * @see java.net.ProxySelector#connectFailed(java.net.URI,
   *      java.net.SocketAddress, java.io.IOException)
   ************************************************************************/
  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    // Not used.
  }

  /*************************************************************************
   * select
   * 
   * @see java.net.ProxySelector#select(java.net.URI)
   ************************************************************************/
  @Override
  public List<Proxy> select(URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("URI must not be null.");
    }

    // Fix for Java 1.6.16+ where we get a infinite loop because
    // URL.connect(Proxy.NO_PROXY) does not work as expected.
    if (!enabled) {
      return ProxyUtil.noProxyList();
    }

    return findProxy(uri);
  }

  /*************************************************************************
   * Evaluation of the given URL with the PAC-file.
   * 
   * Two cases can be handled here: DIRECT Fetch the object directly from the
   * content HTTP server denoted by its URL PROXY name:port Fetch the object via
   * the proxy HTTP server at the given location (name and port)
   * 
   * @param uri
   *          <code>URI</code> to be evaluated.
   * @return <code>Proxy</code>-object list as result of the evaluation.
   ************************************************************************/

  private List<Proxy> findProxy(URI uri) {
    try {
      if (pacScriptParser == null) {
        return ProxyUtil.noProxyList();
      }
      String parseResult = pacScriptParser.evaluate(uri.toString(), uri.getHost());
      if (parseResult == null) {
        return ProxyUtil.noProxyList();
      }
      List<Proxy> proxies = new ArrayList<Proxy>();
      String[] proxyDefinitions = parseResult.split("[;]");
      for (String proxyDef : proxyDefinitions) {
        if (proxyDef.trim().length() > 0) {
          proxies.add(buildProxyFromPacResult(proxyDef));
        }
      }
      return proxies;
    } catch (ProxyEvaluationException e) {
      Logger.log(getClass(), LogLevel.ERROR, "PAC resolving error.", e);
      return ProxyUtil.noProxyList();
    }
  }

  /*************************************************************************
   * The proxy evaluator will return a proxy string. This method will take this
   * string and build a matching <code>Proxy</code> for it.
   * 
   * @param pacResult
   *          the result from the PAC parser.
   * @return a Proxy
   ************************************************************************/

  private Proxy buildProxyFromPacResult(String pacResult) {
    String[] words = pacResult.trim().split("\\s+");

    if (words.length == 0) return Proxy.NO_PROXY;
    if (words.length == 1) return Proxy.NO_PROXY;

    String proxyType = words[0];
    String host  = concat(words,1);

    // Check proxy type.
    Proxy.Type type = Proxy.Type.HTTP;
    if (proxyType.toUpperCase().startsWith(PAC_SOCKS)) {
      type = Proxy.Type.SOCKS;
    }

    Integer port = ProxyUtil.DEFAULT_PROXY_PORT;

    // Split port from host
    int indexOfPort = host.indexOf(':');
    int index2 = host.lastIndexOf(']');
    if (indexOfPort != -1 && index2 < indexOfPort) {
      port = Integer.parseInt(host.substring(indexOfPort + 1).trim());
      host = host.substring(0, indexOfPort).trim();
    }

    SocketAddress adr = InetSocketAddress.createUnresolved(host, port);
    return new Proxy(type, adr);
  }
  private static String concat(String[] strings, int startIndex){
    StringBuilder b = new StringBuilder();
    for (int i = startIndex; i < strings.length; i++) {
      b.append(strings[i]);
    }
    return b.toString();

  }
}
