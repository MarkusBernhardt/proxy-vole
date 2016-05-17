package com.github.markusbernhardt.proxy.search.desktop.win;

import java.net.ProxySelector;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.browser.ie.IEProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Extracts the proxy settings from the windows registry. This will read the
 * windows system proxy settings.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class WinProxySearchStrategy implements ProxySearchStrategy {

    /*************************************************************************
     * Constructor
     ************************************************************************/

    public WinProxySearchStrategy() {
        super();
    }

    /*************************************************************************
     * getProxySelector
     * 
     * @see com.github.markusbernhardt.proxy.ProxySearchStrategy#getProxySelector()
     ************************************************************************/

    @Override
    public ProxySelector getProxySelector() throws ProxyException {
        // TODO Rossi 08.05.2009 Implement this by using Win API calls.
        // new Win32ProxyUtils().winHttpGetDefaultProxyConfiguration()
        // Current fallback is to use the IE settings. This is better
        // because the registry settings are most of the time not set.
        // Some Windows server installations may use it though.
        return new IEProxySearchStrategy().getProxySelector();
    }

    /*************************************************************************
     * Gets the printable name of the search strategy.
     *  
     * @return the printable name of the search strategy
     ************************************************************************/

    @Override
    public String getName() {
        return "windows";
    }

}
