package com.github.markusbernhardt.proxy.selector.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.Proxy;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;

/*****************************************************************************
 * Unit Tests for the ProtocolDispatchSelector
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class ProtocolDispatchTest {

	private static ProtocolDispatchSelector ps;

	@BeforeClass
	public static void setup() {
		ps = new ProtocolDispatchSelector();
		ps.setSelector("http", new FixedProxySelector(TestUtil.HTTP_TEST_PROXY));
		ps.setSelector("https", new FixedProxySelector(TestUtil.HTTPS_TEST_PROXY));
		ps.setSelector("ftp", new FixedProxySelector(TestUtil.FTP_TEST_PROXY));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testDispatchHttp() {
		List<Proxy> result = ps.select(TestUtil.HTTP_TEST_URI);
		assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testDispatchHttps() {
		List<Proxy> result = ps.select(TestUtil.HTTPS_TEST_URI);
		assertEquals(TestUtil.HTTPS_TEST_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testDispatchFtp() {
		List<Proxy> result = ps.select(TestUtil.FTP_TEST_URI);
		assertEquals(TestUtil.FTP_TEST_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testRemove() {
		ProtocolDispatchSelector px = new ProtocolDispatchSelector();
		FixedProxySelector selector = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
		px.setSelector("http", selector);
		assertEquals(selector, px.getSelector("http"));
		px.removeSelector("http");
		assertNull(px.getSelector("http"));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testFallback() {
		ProtocolDispatchSelector px = new ProtocolDispatchSelector();
		FixedProxySelector selector = new FixedProxySelector(TestUtil.HTTP_TEST_PROXY);
		px.setFallbackSelector(selector);

		List<Proxy> proxies = px.select(TestUtil.HTTP_TEST_URI);

		assertEquals(TestUtil.HTTP_TEST_PROXY, proxies.get(0));
	}

}
