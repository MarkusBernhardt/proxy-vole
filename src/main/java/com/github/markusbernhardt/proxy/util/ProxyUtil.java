package com.github.markusbernhardt.proxy.util;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector;
import com.github.markusbernhardt.proxy.selector.pac.PacScriptSource;
import com.github.markusbernhardt.proxy.selector.pac.UrlPacScriptSource;

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
     * 
     * @param proxyVar
     *            the proxy string. example: http://192.168.10.9:8080/
     * @return a FixedProxySelector using this settings, null on parse error.
     ************************************************************************/

    public static FixedProxySelector parseProxySettings(String proxyVar) {
        if (proxyVar == null || proxyVar.trim().length() == 0) {
            return null;
        }

        try {
            // Protocol missing then assume http and provide it
            if (proxyVar.indexOf(":/") == -1) {
                proxyVar = "http://" + proxyVar;
            }

            URL url = new URL(proxyVar);
            String host = cleanIPv6(url.getHost());

            int port = url.getPort();
            if (port == -1) {
                port = DEFAULT_PROXY_PORT;
            }
            return new FixedProxySelector(host.trim(), port);
        } catch (MalformedURLException e) {
            Logger.log(ProxyUtil.class, Logger.LogLevel.WARNING, "Cannot parse Proxy Settings {0}", proxyVar);
            return null;
        }
    }

    /*************************************************************************
     * Gets an unmodifiable proxy list that will have as it's only entry an
     * DIRECT proxy.
     * 
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

    /*************************************************************************
     * Build a PAC proxy selector for the given URL.
     * 
     * @param url
     *            to fetch the PAC script from.
     * @return a PacProxySelector or null if it is not possible to build a
     *         working selector.
     ************************************************************************/

    public static PacProxySelector buildPacSelectorForUrl(String url) {
        PacProxySelector result = null;
        PacScriptSource pacSource = new UrlPacScriptSource(url);
        if (pacSource.isScriptValid()) {
            result = new PacProxySelector(pacSource);
        }
        return result;
    }

    /*************************************************************************
     * This method can be used to cleanup an IPv6 address. It will remove the
     * surrounding square brackets if found. e.g. [2001:4860:0:2001::68] will be
     * returned as 2001:4860:0:2001::68
     * 
     * @param hostOrIP
     *            to cleanup
     * @return the raw host or IP without any IPv6 brackets.
     ************************************************************************/

    public static String cleanIPv6(String hostOrIP) {
        if (hostOrIP == null) {
            return null;
        }
        hostOrIP = hostOrIP.trim();
        if (hostOrIP.startsWith("[")) {
            hostOrIP = hostOrIP.substring(1);
        }
        if (hostOrIP.endsWith("]")) {
            hostOrIP = hostOrIP.substring(0, hostOrIP.length() - 1);
        }
        return hostOrIP;
    }

}
