package com.btr.proxy.search.java;

import static org.junit.Assert.*;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.btr.proxy.TestUtil;


/*****************************************************************************
 * Unit tests for the Java proxy search strategy. 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class JavaProxySearchTest {

	private ProxySelector selector;

	/*************************************************************************
	 * Setup before the tests.
	 ************************************************************************/
	@Before
	public void setup() {
		System.setProperty("http.proxyHost", "http_proxy.unit-test.invalid");
		System.setProperty("http.proxyPort", "8090");
		System.setProperty("https.proxyHost", "https_proxy.unit-test.invalid");
		System.setProperty("https.proxyPort", "8091");
		System.setProperty("ftp.proxyHost", "ftp_proxy.unit-test.invalid");
		System.setProperty("ftp.proxyPort", "8092");
		System.setProperty("socksProxyHost", "socks_proxy.unit-test.invalid");
		System.setProperty("socksProxyPort", "8095");
		
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
	 ************************************************************************/
	@Test
	public void testHTTPS() {
		List<Proxy> result = this.selector.select(TestUtil.HTTPS_TEST_URI);
		assertEquals(TestUtil.HTTPS_TEST_PROXY, result.get(0));
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
	 ************************************************************************/
	@Test
	public void testSOCKS() {
		List<Proxy> result = this.selector.select(TestUtil.SOCKS_TEST_URI);
		assertEquals(TestUtil.SOCKS_TEST_PROXY, result.get(0));
	}
	
}

