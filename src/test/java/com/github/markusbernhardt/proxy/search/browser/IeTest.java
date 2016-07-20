package com.github.markusbernhardt.proxy.search.browser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.search.browser.ie.IELocalByPassFilter;
import com.github.markusbernhardt.proxy.search.browser.ie.IEProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.UriFilter;
import com.github.markusbernhardt.proxy.util.PlatformUtil.Platform;

/*****************************************************************************
 * Unit tests for the InternetExplorer search. Only limited testing as this only
 * runs on windwos and needs a installed IE and IE proxy settings written to the
 * registry.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class IeTest {

	/*************************************************************************
	 * Test method.
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 ************************************************************************/
	@Test
	public void testInvoke() throws ProxyException {
		if (Platform.WIN.equals(PlatformUtil.getCurrentPlattform())) {
			IEProxySearchStrategy st = new IEProxySearchStrategy();

			// Try at least to invoke it and test if the dll does not crash
			st.getProxySelector();
		}
	}

	/*************************************************************************
	 * Test method.
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws URISyntaxException
	 *             if url syntax is wrong.
	 * @throws MalformedURLException
	 *             on wrong url format.
	 ************************************************************************/
	@Test
	public void testLocalByPassFilter() throws ProxyException, MalformedURLException, URISyntaxException {
		UriFilter filter = new IELocalByPassFilter();
		assertTrue(filter.accept(TestUtil.LOCAL_TEST_URI));
		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
		assertFalse(filter.accept(new URL("http://123.45.55.6").toURI()));
	}

}
