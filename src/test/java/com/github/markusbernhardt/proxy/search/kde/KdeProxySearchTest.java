package com.github.markusbernhardt.proxy.search.kde;

import static org.junit.Assert.assertEquals;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.search.desktop.kde.KdeProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Unit tests for the KDE settings search strategy. For every test the
 * "user.home" system property is switched to the test/data folder where we
 * provide some KDE config files prepared for the test cases.
 * 
 * If the env tests fail you need to set the followingenvironment variables:
 * <p>
 * HTTP_PROXY = http://http_proxy.unit-test.invalid:8090 <br>
 * HTTPS_PROXY = http://https_proxy.unit-test.invalid:8091 <br>
 * FTP_PROXY = http://ftp_proxy.unit-test.invalid:8092 <br>
 * </p>
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class KdeProxySearchTest {

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     ************************************************************************/
    @Test
    public void testNone() throws ProxyException {
        TestUtil.setTestDataFolder("kde_none");
        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();
        List<Proxy> result = ps.select(TestUtil.HTTP_TEST_URI);

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
        TestUtil.setTestDataFolder("kde_manual");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

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
        TestUtil.setTestDataFolder("kde_manual");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

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
        TestUtil.setTestDataFolder("kde_manual");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

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
    public void testPac() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("kde_pac_script");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

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
    @Ignore
    public void testEnvHttp() throws ProxyException, URISyntaxException {
        // There is no good was to initialize environment variables in the
        // running process.
        // System.getenv().put("HTTP_PROXY",
        // "http://http_proxy.unit-test.invalid:8090"); // Does not work
        TestUtil.setTestDataFolder("kde_env");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

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
    @Ignore
    public void testEnvHttps() throws ProxyException, URISyntaxException {
        // There is no good was to initialize environment variables in the
        // running process.
        // System.getenv().put("HTTPS_PROXY",
        // "http://http_proxy.unit-test.invalid:8090"); // Does not work
        TestUtil.setTestDataFolder("kde_env");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

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
    @Ignore
    public void testEnvFtp() throws ProxyException, URISyntaxException {
        // there is no good was to initialize environment variables in the
        // running process.
        // System.getenv().put("FTP_PROXY",
        // "http://http_proxy.unit-test.invalid:8090"); // Does not work
        TestUtil.setTestDataFolder("kde_env");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

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
    public void testWhiteList() throws ProxyException, URISyntaxException {
        TestUtil.setTestDataFolder("kde_white_list");

        ProxySelector ps = new KdeProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.NO_PROXY_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

}
