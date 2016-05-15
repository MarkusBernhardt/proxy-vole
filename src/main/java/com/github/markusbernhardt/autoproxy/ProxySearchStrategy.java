package com.github.markusbernhardt.autoproxy;

import java.net.ProxySelector;

import com.github.markusbernhardt.autoproxy.util.ProxyException;

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
     ************************************************************************/

    public ProxySelector getProxySelector() throws ProxyException;

}
