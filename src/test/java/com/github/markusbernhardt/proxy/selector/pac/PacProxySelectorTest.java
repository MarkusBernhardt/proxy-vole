package com.github.markusbernhardt.proxy.selector.pac;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector;
import com.github.markusbernhardt.proxy.selector.pac.PacScriptMethods;
import com.github.markusbernhardt.proxy.selector.pac.UrlPacScriptSource;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Tests for the Pac script parser and proxy selector.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class PacProxySelectorTest {

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testScriptExecution() throws ProxyException, MalformedURLException {
		List<Proxy> result = new PacProxySelector(new UrlPacScriptSource(toUrl("test1.pac")))
		        .select(TestUtil.HTTP_TEST_URI);

		assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testScriptExecution2() throws ProxyException, MalformedURLException {
		PacProxySelector pacProxySelector = new PacProxySelector(new UrlPacScriptSource(toUrl("test2.pac")));
		List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));

		result = pacProxySelector.select(TestUtil.HTTPS_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test download fix to prevent infinite loop.
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void pacDownloadFromURLShouldNotUseProxy() throws ProxyException, MalformedURLException {
		ProxySelector oldOne = ProxySelector.getDefault();
		try {
			ProxySelector.setDefault(new ProxySelector() {
				@Override
				public List<Proxy> select(URI uri) {
					throw new IllegalStateException("Should not download via proxy");
				}

				@Override
				public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
					// Not used
				}
			});

			PacProxySelector pacProxySelector = new PacProxySelector(
			        new UrlPacScriptSource("http://www.test.invalid/wpad.pac"));
			pacProxySelector.select(TestUtil.HTTPS_TEST_URI);
		} finally {
			ProxySelector.setDefault(oldOne);
		}
	}

	/*************************************************************************
	 * Test method
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testScriptMuliProxy() throws ProxyException, MalformedURLException {
		PacProxySelector pacProxySelector = new PacProxySelector(new UrlPacScriptSource(toUrl("testMultiProxy.pac")));
		List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
		assertEquals(2, result.size());
		assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy.com", 80)), result.get(0));
		assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy2.com", 8080)), result.get(1));
	}

	/*************************************************************************
	 * Test method for the override local IP feature.
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws MalformedURLException
	 *             on URL erros
	 ************************************************************************/
	@Test
	public void testLocalIPOverride() throws ProxyException, MalformedURLException {
		System.setProperty(PacScriptMethods.OVERRIDE_LOCAL_IP, "123.123.123.123");
		try {
			PacProxySelector pacProxySelector = new PacProxySelector(new UrlPacScriptSource(toUrl("testLocalIP.pac")));
			List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
			assertEquals(result.get(0),
			        new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("123.123.123.123", 8080)));
		} finally {
			System.setProperty(PacScriptMethods.OVERRIDE_LOCAL_IP, "");
		}

	}

	/*************************************************************************
	 * Helper method to build the url to the given test file
	 * 
	 * @param testFile
	 *            the name of the test file.
	 * @return the URL.
	 * @throws MalformedURLException
	 ************************************************************************/

	private String toUrl(String testFile) throws MalformedURLException {
		return new File(TestUtil.TEST_DATA_FOLDER + "pac", testFile).toURI().toURL().toString();
	}

}
