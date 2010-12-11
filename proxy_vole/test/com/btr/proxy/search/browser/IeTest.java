package com.btr.proxy.search.browser;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.btr.proxy.TestUtil;
import com.btr.proxy.search.browser.ie.IELocalByPassFilter;
import com.btr.proxy.search.browser.ie.IEProxySearchStrategy;
import com.btr.proxy.util.PlatformUtil;
import com.btr.proxy.util.ProxyException;
import com.btr.proxy.util.PlatformUtil.Platform;
import com.btr.proxy.util.UriFilter;

/*****************************************************************************
 *  Unit tests for the InternetExplorer search.
 *  Only limited testing as this only runs on windwos and needs a 
 *  installed IE and IE proxy settings written to the registry.
 *  
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class IeTest {

	/*************************************************************************
	 * Test method.
	 * @throws ProxyException on proxy detection error.
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
	 * @throws ProxyException on proxy detection error.
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 ************************************************************************/
	@Test
	public void testLocalByPassFilter() throws ProxyException, MalformedURLException, URISyntaxException {
		UriFilter filter = new IELocalByPassFilter();
		assertTrue(filter.accept(TestUtil.LOCAL_TEST_URI));
		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
		assertFalse(filter.accept(new URL("http://123.45.55.6").toURI()));
	}

}

