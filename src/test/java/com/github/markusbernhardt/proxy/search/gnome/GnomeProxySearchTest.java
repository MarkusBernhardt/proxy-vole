package com.github.markusbernhardt.proxy.search.gnome;

import static org.junit.Assert.assertEquals;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.search.desktop.gnome.GnomeProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Unit tests for the Gnome settings search strategy. For every test the
 * "user.home" system property is switched to the test/data folder where we
 * provide some Gnome config files prepared for the test cases.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class GnomeProxySearchTest {

    /*************************************************************************
     * Test method
     * 
     * @throws ProxyException
     *             on proxy detection error.
     ************************************************************************/
    @Test
    public void testNone() throws ProxyException {
        TestUtil.setTestDataFolder("gnome_none");
        ProxySelector ps = new GnomeProxySearchStrategy().getProxySelector();
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
        TestUtil.setTestDataFolder("gnome_manual");

        ProxySelector ps = new GnomeProxySearchStrategy().getProxySelector();

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
        TestUtil.setTestDataFolder("gnome_manual");

        ProxySelector ps = new GnomeProxySearchStrategy().getProxySelector();

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
        TestUtil.setTestDataFolder("gnome_manual");

        ProxySelector ps = new GnomeProxySearchStrategy().getProxySelector();

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
        TestUtil.setTestDataFolder("gnome_pac_script");

        ProxySelector ps = new GnomeProxySearchStrategy().getProxySelector();

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
        TestUtil.setTestDataFolder("gnome_white_list");

        ProxySelector ps = new GnomeProxySearchStrategy().getProxySelector();

        List<Proxy> result = ps.select(TestUtil.NO_PROXY_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

}
