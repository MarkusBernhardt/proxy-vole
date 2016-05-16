package com.github.markusbernhardt.proxy.search.desktop;

import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.search.desktop.DesktopProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Unit tests for the desktop facade search strategy.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class DesktopProxySearchTest {

    /*************************************************************************
     * Test method.
     * 
     * @throws ProxyException
     *             on error.
     ************************************************************************/
    @Test
    public void testDesktopStrategsIsWorking() throws ProxyException {
        new DesktopProxySearchStrategy().getProxySelector();
    }

    /*************************************************************************
     * Test method.
     * 
     * @throws URISyntaxException
     *             on error parsing the URI.
     * @throws ProxyException
     *             on selector error.
     ************************************************************************/
    @Test
    public void emptyURIShouldNotRaiseNPE() throws URISyntaxException, ProxyException {
        ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
        ProxySelector myProxySelector = proxySearch.getProxySelector();
        if (myProxySelector != null) {
            myProxySelector.select(new URI(""));
        }
    }

}
