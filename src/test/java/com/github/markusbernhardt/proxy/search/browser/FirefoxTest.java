package com.github.markusbernhardt.proxy.search.browser;

import static org.junit.Assert.assertEquals;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.search.browser.firefox.FirefoxProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Unit tests for the firefox search.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class FirefoxTest {

    /*************************************************************************
     * Setup environment for tests.
     ************************************************************************/
    @BeforeClass
    public static void setup() {
        // Fake the OS for this tests.
        System.setProperty("os.name", "Linux");
    }

    /*************************************************************************
     * Test method.
     * 
     * @throws ProxyException
     *             on error.
     ************************************************************************/
    @Test
    public void testNone() throws ProxyException {
        TestUtil.setTestDataFolder("ff3_none");

        FirefoxProxySearchStrategy ff = new FirefoxProxySearchStrategy();
        ProxySelector ps = ff.getProxySelector();

        List<Proxy> result = ps.select(TestUtil.HTTPS_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));

    }

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testManualHttp() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("ff3_manual");

        ProxySelector ps = new FirefoxProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.HTTP_TEST_URI);
        assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testManualHttps() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("ff3_manual");

        ProxySelector ps = new FirefoxProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.HTTPS_TEST_URI);
        assertEquals(TestUtil.HTTPS_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testManualFtp() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("ff3_manual");

        ProxySelector ps = new FirefoxProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.FTP_TEST_URI);
        assertEquals(TestUtil.FTP_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testManualSocks() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("ff3_manual");

        ProxySelector ps = new FirefoxProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.SOCKS_TEST_URI);
        assertEquals(TestUtil.SOCKS_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testPac() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("ff3_pac_script");

        ProxySelector ps = new FirefoxProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.HTTP_TEST_URI);
        assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     * @throws URISyntaxException
     *             on invalid URL syntax.
     ************************************************************************/
    @Test
    public void testWhiteList() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("ff3_white_list");

        ProxySelector ps = new FirefoxProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.NO_PROXY_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

}
