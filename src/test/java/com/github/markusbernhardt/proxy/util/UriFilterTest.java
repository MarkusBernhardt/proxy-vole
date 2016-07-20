package com.github.markusbernhardt.proxy.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.selector.whitelist.HostnameFilter;
import com.github.markusbernhardt.proxy.selector.whitelist.IpRangeFilter;
import com.github.markusbernhardt.proxy.selector.whitelist.HostnameFilter.Mode;
import com.github.markusbernhardt.proxy.util.UriFilter;

/*****************************************************************************
 * Some unit tests for the UriFilter class.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class UriFilterTest {

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter1() {
		UriFilter filter = new HostnameFilter(Mode.BEGINS_WITH, "no_proxy");

		assertTrue(filter.accept(TestUtil.NO_PROXY_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter2() {
		UriFilter filter = new HostnameFilter(Mode.BEGINS_WITH, "no_proxy");

		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter3() throws URISyntaxException {
		UriFilter filter = new HostnameFilter(Mode.BEGINS_WITH, "192.168.0");

		assertTrue(filter.accept(new URI("http://192.168.0.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter4() throws URISyntaxException {
		UriFilter filter = new HostnameFilter(Mode.BEGINS_WITH, "192.168.0");

		assertFalse(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/
	@Test
	public void testBeginsWithFilter() {
		UriFilter filter = new HostnameFilter(Mode.BEGINS_WITH, "no_proxy");

		assertTrue(filter.accept(TestUtil.NO_PROXY_TEST_URI));
		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testEndsWithFilter() throws URISyntaxException {
		UriFilter filter = new HostnameFilter(Mode.ENDS_WITH, ".unit-test.invalid");

		assertTrue(filter.accept(TestUtil.NO_PROXY_TEST_URI));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testEndsWithFilter2() throws URISyntaxException {
		UriFilter filter = new HostnameFilter(Mode.ENDS_WITH, ".unit-test.invalid");

		assertFalse(filter.accept(new URI("http://test.no-host.invalid:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testEndsWithFilter3() throws URISyntaxException {
		UriFilter filter = new HostnameFilter(Mode.ENDS_WITH, ".100");

		assertTrue(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testIpRangeFilter() throws URISyntaxException {
		UriFilter filter = new IpRangeFilter("192.168.0.0/24");

		assertTrue(filter.accept(new URI("http://192.168.0.100:81/test.data")));
		assertFalse(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testIp6RangeFilter() throws URISyntaxException {
		UriFilter filter = new IpRangeFilter("2001:4860:0:2001::/24");

		assertTrue(filter.accept(new URI("http://[2001:4860:0:2001::68]:81/test.data")));
		assertFalse(filter.accept(new URI("http://[3001:4860:0:2001::68]:81/test.data")));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws URISyntaxException
	 *             on invalid URL syntax.
	 ************************************************************************/
	@Test
	public void testWithProtocolFilter() throws URISyntaxException {
		UriFilter filter = new HostnameFilter(Mode.BEGINS_WITH, "http://192.168.0.100");

		assertTrue(filter.accept(new URI("http://192.168.0.100:81/test.data")));
		assertFalse(filter.accept(new URI("ftp://192.168.0.100:81/test.data")));
		assertFalse(filter.accept(new URI("http://192.168.1.100:81/test.data")));
	}

}
