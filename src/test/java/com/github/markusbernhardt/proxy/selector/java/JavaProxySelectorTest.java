package com.github.markusbernhardt.proxy.selector.java;

import static org.junit.Assert.assertNull;

import java.net.ProxySelector;

import org.junit.Test;

import com.github.markusbernhardt.proxy.search.java.JavaProxySearchStrategy;

/*****************************************************************************
 * Some unit tests for the Java Proxy search strategy.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class JavaProxySelectorTest {

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void withoutSystemPropertyShouldReturnNull() {
        ProxySelector ps = new JavaProxySearchStrategy().getProxySelector();
        assertNull(ps);
    }

}
