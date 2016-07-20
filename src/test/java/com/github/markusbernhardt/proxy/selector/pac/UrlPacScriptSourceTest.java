package com.github.markusbernhardt.proxy.selector.pac;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.markusbernhardt.proxy.selector.pac.UrlPacScriptSource;

/*****************************************************************************
 * Tests for the UrlPacScriptSource.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class UrlPacScriptSourceTest {

	/*************************************************************************
	 * Unit Test
	 ************************************************************************/
	@Test
	public void testHttpCharsetParser() {
		UrlPacScriptSource scriptSource = new UrlPacScriptSource("");
		String charset = scriptSource.parseCharsetFromHeader("application/x-ns-proxy-autoconfig; charset=UTF-8");
		assertEquals("UTF-8", charset);
	}

	/*************************************************************************
	 * Unit Test
	 ************************************************************************/
	@Test
	public void testHttpCharsetParserDefault() {
		UrlPacScriptSource scriptSource = new UrlPacScriptSource("");
		String charset = scriptSource.parseCharsetFromHeader("application/octet-stream;");
		assertEquals("ISO-8859-1", charset);
	}

	/*************************************************************************
	 * Unit Test
	 ************************************************************************/
	@Test
	public void overrideTimeoutShouldWork() {
		System.setProperty(UrlPacScriptSource.OVERRIDE_CONNECT_TIMEOUT, "5000");
		UrlPacScriptSource scriptSource = new UrlPacScriptSource("");
		int timeout = scriptSource.getTimeOut(UrlPacScriptSource.OVERRIDE_CONNECT_TIMEOUT, 1000);
		System.clearProperty(UrlPacScriptSource.OVERRIDE_CONNECT_TIMEOUT);
		assertEquals(5000, timeout);
	}

	/*************************************************************************
	 * Unit Test
	 ************************************************************************/
	@Test
	public void timeoutShouldUseDefault() {
		System.setProperty(UrlPacScriptSource.OVERRIDE_CONNECT_TIMEOUT, "XXX");
		UrlPacScriptSource scriptSource = new UrlPacScriptSource("");
		int timeout = scriptSource.getTimeOut(UrlPacScriptSource.OVERRIDE_CONNECT_TIMEOUT, 1000);
		System.clearProperty(UrlPacScriptSource.OVERRIDE_CONNECT_TIMEOUT);
		assertEquals(1000, timeout);
	}

}
