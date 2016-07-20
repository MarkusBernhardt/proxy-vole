package com.github.markusbernhardt.proxy.selector.pac;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector;
import com.github.markusbernhardt.proxy.selector.pac.UrlPacScriptSource;

/**
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 */
public class PacPerProtocolTest {

	/*************************************************************************
	 * Test the PAC selector for a given protocol.
	 * 
	 * @throws IOException
	 *             of read error.
	 * @throws URISyntaxException
	 *             on uri syntax error.
	 ************************************************************************/
	@Test
	public void testPacForSocket() throws IOException, URISyntaxException {

		new URI("socket://host1.unit-test.invalid/");

		List<Proxy> result = new PacProxySelector(new UrlPacScriptSource(toUrl("test1.pac")))
		        .select(TestUtil.SOCKET_TEST_URI);

		assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
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
