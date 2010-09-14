package com.btr.proxy.selector.pac;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/*****************************************************************************
 * Tests for the UrlPacScriptSource. 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
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

}

