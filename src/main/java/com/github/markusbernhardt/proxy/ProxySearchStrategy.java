package com.github.markusbernhardt.proxy;

import java.net.ProxySelector;

import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Interface for a proxy search strategy.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public interface ProxySearchStrategy {

    /*************************************************************************
     * Gets the a ProxySelector found by applying the search strategy.
     * 
     * @return a ProxySelector, null if none is found.
     * @throws ProxyException
     *             on error
     ************************************************************************/

    public ProxySelector getProxySelector() throws ProxyException;

    /*************************************************************************
     * Gets the printable name of the search strategy.
     * 
     * @return the printable name of the search strategy
     ************************************************************************/

    public String getName();
}
