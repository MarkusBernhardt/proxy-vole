package com.btr.proxy.selector.pac;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.List;

import org.junit.Test;

import com.btr.proxy.TestUtil;
import com.btr.proxy.util.ProxyException;


/*****************************************************************************
 * Tests for the Pac script parser and proxy selector. 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class PacProxySelectorTest {

	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws MalformedURLException on URL erros 
	 ************************************************************************/
	@Test
	public void testScriptExecution() throws ProxyException, MalformedURLException {
		List<Proxy> result = new PacProxySelector(
				new UrlPacScriptSource(toUrl("test1.pac"))).select(TestUtil.HTTP_TEST_URI);
		
		assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test method
	 * @throws ProxyException on proxy detection error.
	 * @throws MalformedURLException on URL erros 
	 ************************************************************************/
	@Test
	public void testScriptExecution2() throws ProxyException, MalformedURLException {
		PacProxySelector pacProxySelector = new PacProxySelector(
				new UrlPacScriptSource(toUrl("test2.pac")));
		List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));
		
		result = pacProxySelector.select(TestUtil.HTTPS_TEST_URI);
		assertEquals(Proxy.NO_PROXY, result.get(0));

		
	}
	
	/*************************************************************************
	 * Helper method to build the url to the given test file
	 * @param testFile the name of the test file.
	 * @return the URL. 
	 * @throws MalformedURLException
	 ************************************************************************/
	
	private String toUrl(String testFile) throws MalformedURLException {
		return new File(TestUtil.TEST_DATA_FOLDER+"pac", testFile).toURI().toURL().toString();
	}


}

