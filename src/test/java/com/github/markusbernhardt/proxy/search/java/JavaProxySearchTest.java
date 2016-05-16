package com.github.markusbernhardt.proxy.search.java;

import static org.junit.Assert.assertEquals;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.search.java.JavaProxySearchStrategy;

/*****************************************************************************
 * Unit tests for the Java proxy search strategy.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class JavaProxySearchTest {

    private ProxySelector selector;

    /*************************************************************************
     * Setup before the tests.
     ************************************************************************/
    @BeforeClass
    public static void setupClass() {
        System.setProperty("http.proxyHost", "http_proxy.unit-test.invalid");
        System.setProperty("http.proxyPort", "8090");
        System.setProperty("http.nonProxyHosts", "no_proxy.unit-test.invalid");
        System.setProperty("https.proxyHost", "https_proxy.unit-test.invalid");
        System.setProperty("https.proxyPort", "8091");
        System.setProperty("ftp.proxyHost", "ftp_proxy.unit-test.invalid");
        System.setProperty("ftp.nonProxyHosts", "no_proxy.unit-test.invalid");
        System.setProperty("ftp.proxyPort", "8092");
        System.setProperty("socksProxyHost", "socks_proxy.unit-test.invalid");
        System.setProperty("socksProxyPort", "8095");
    }

    /*************************************************************************
     * Setup before the tests.
     ************************************************************************/
    @AfterClass
    public static void teardownClass() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.nonProxyHosts");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("ftp.proxyHost");
        System.clearProperty("ftp.nonProxyHosts");
        System.clearProperty("ftp.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
    }

    /*************************************************************************
     * Setup before every single test
     ************************************************************************/
    @Before
    public void setup() {
        this.selector = new JavaProxySearchStrategy().getProxySelector();
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testHTTP() {
        List<Proxy> result = this.selector.select(TestUtil.HTTP_TEST_URI);
        assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on wrong URI.
     ************************************************************************/
    @Test
    public void testHTTPnoProxy() throws URISyntaxException {
        List<Proxy> result = this.selector.select(new URI("http://no_proxy.unit-test.invalid"));
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testHTTPS() {
        List<Proxy> result = this.selector.select(TestUtil.HTTPS_TEST_URI);
        assertEquals(TestUtil.HTTPS_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on wrong URI.
     ************************************************************************/
    @Test
    public void testHTTPSnoProxy() throws URISyntaxException {
        List<Proxy> result = this.selector.select(new URI("https://no_proxy.unit-test.invalid"));
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testFTP() {
        List<Proxy> result = this.selector.select(TestUtil.FTP_TEST_URI);
        assertEquals(TestUtil.FTP_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     * 
     * @throws URISyntaxException
     *             on wrong URI.
     ************************************************************************/
    @Test
    public void testFTPnoProxy() throws URISyntaxException {
        List<Proxy> result = this.selector.select(new URI("ftp://no_proxy.unit-test.invalid"));
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     ************************************************************************/
    @Test
    public void testSOCKS() {
        List<Proxy> result = this.selector.select(TestUtil.SOCKS_TEST_URI);
        assertEquals(TestUtil.SOCKS_TEST_PROXY, result.get(0));
    }

}
