package com.github.markusbernhardt.proxy.selector.whitelist;

import static org.junit.Assert.assertEquals;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;

/*****************************************************************************
 * Some unit tests for the white list selector.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class NoProxyTest {

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testWhiteList() {
        ProxySelector delegate = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
        ProxyBypassListSelector ps = new ProxyBypassListSelector("no_prox.*", delegate);

        assertEquals(delegate.select(TestUtil.HTTP_TEST_URI).get(0), ps.select(TestUtil.HTTP_TEST_URI).get(0));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testWhiteList2() {
        ProxySelector delegate = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
        ProxyBypassListSelector ps = new ProxyBypassListSelector("*.unit-test.invalid", delegate);

        List<Proxy> result = ps.select(TestUtil.HTTP_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testWhiteList3() throws URISyntaxException {
        ProxySelector delegate = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
        ProxyBypassListSelector ps = new ProxyBypassListSelector("*.unit-test.invalid, localhost, 127.0.0.1", delegate);

        List<Proxy> result = ps.select(new URI("http://localhost:65/getDocument"));
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = ps.select(new URI("http://127.0.0.1:65/getDocument"));
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testWhiteList4() {
        ProxySelector delegate = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
        ProxyBypassListSelector ps = new ProxyBypassListSelector("*.unit-test.invalid, ", delegate);

        List<Proxy> result = ps.select(TestUtil.HTTP_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testWhiteList5() throws URISyntaxException {
        ProxySelector delegate = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
        ProxyBypassListSelector ps = new ProxyBypassListSelector("*.unit-test.invalid localhost 127.0.0.1", delegate);

        List<Proxy> result = ps.select(new URI("http://localhost:65/getDocument"));
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = ps.select(new URI("http://127.0.0.1:65/getDocument"));
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testIpRange() throws URISyntaxException {
        ProxySelector delegate = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
        ProxyBypassListSelector ps = new ProxyBypassListSelector("192.168.0.0/24", delegate);

        List<Proxy> result = ps.select(new URI("http://192.168.0.100:81/test.data"));
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = ps.select(new URI("http://192.168.1.100:81/test.data"));
        assertEquals(delegate.select(TestUtil.HTTP_TEST_URI).get(0), result.get(0));
    }

    /*************************************************************************
     * Test method for issue 31
     * 
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void ipRangeShouldNotMatchHttp() throws URISyntaxException {
        ProxySelector delegate = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
        ProxyBypassListSelector ps = new ProxyBypassListSelector("http://192.*", delegate);

        List<Proxy> result = ps.select(new URI("http://192.168.0.100:81/test.data"));
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

}
