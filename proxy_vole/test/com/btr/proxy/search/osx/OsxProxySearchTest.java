package com.btr.proxy.search.osx;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.btr.proxy.TestUtil;
import com.btr.proxy.search.desktop.osx.OsxProxySearchStrategy;
import com.btr.proxy.util.ProxyException;


/*****************************************************************************
 * Unit tests for the OSX settings search strategy.
 * 
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class OsxProxySearchTest {
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws URISyntaxException on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testManualHttp() throws ProxyException, URISyntaxException {
		System.setProperty(OsxProxySearchStrategy.OVERRIDE_SETTINGS_FILE, "test"+File.separator+"data"+File.separator+"osx"+File.separator+"osx_manual.plist");
		ProxySelector ps = new OsxProxySearchStrategy().getProxySelector();
		List<Proxy> result = ps.select(TestUtil.HTTP_TEST_URI);
		assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
	}
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws URISyntaxException on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testManualHttps() throws ProxyException, URISyntaxException {
		System.setProperty(OsxProxySearchStrategy.OVERRIDE_SETTINGS_FILE, "test"+File.separator+"data"+File.separator+"osx"+File.separator+"osx_manual.plist");
		ProxySelector ps = new OsxProxySearchStrategy().getProxySelector();
		List<Proxy> result = ps.select(TestUtil.HTTPS_TEST_URI);
		assertEquals(TestUtil.HTTPS_TEST_PROXY, result.get(0));
	}
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws URISyntaxException on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testManualFtp() throws ProxyException, URISyntaxException {
		System.setProperty(OsxProxySearchStrategy.OVERRIDE_SETTINGS_FILE, "test"+File.separator+"data"+File.separator+"osx"+File.separator+"osx_manual.plist");
		ProxySelector ps = new OsxProxySearchStrategy().getProxySelector();
		List<Proxy> result = ps.select(TestUtil.FTP_TEST_URI);
		assertEquals(TestUtil.FTP_TEST_PROXY, result.get(0));
	}
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws URISyntaxException on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testManualSocks() throws ProxyException, URISyntaxException {
		System.setProperty(OsxProxySearchStrategy.OVERRIDE_SETTINGS_FILE, "test"+File.separator+"data"+File.separator+"osx"+File.separator+"osx_manual.plist");
		ProxySelector ps = new OsxProxySearchStrategy().getProxySelector();
		List<Proxy> result = ps.select(TestUtil.SOCKS_TEST_URI);
		assertEquals(TestUtil.SOCKS_TEST_PROXY, result.get(0));
	}
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws URISyntaxException on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testNoProxyList() throws ProxyException, URISyntaxException {
		System.setProperty(OsxProxySearchStrategy.OVERRIDE_SETTINGS_FILE, "test"+File.separator+"data"+File.separator+"osx"+File.separator+"osx_manual.plist");
		ProxySelector ps = new OsxProxySearchStrategy().getProxySelector();
		List<Proxy> result = ps.select(TestUtil.NO_PROXY_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));
	}
	
	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws URISyntaxException on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testSimpleHostTest() throws ProxyException, URISyntaxException {
		System.setProperty(OsxProxySearchStrategy.OVERRIDE_SETTINGS_FILE, "test"+File.separator+"data"+File.separator+"osx"+File.separator+"osx_manual.plist");
		ProxySelector ps = new OsxProxySearchStrategy().getProxySelector();
		List<Proxy> result = ps.select(TestUtil.LOCAL_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));
	}

}
