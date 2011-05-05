package com.btr.proxy.search.desktop;

import org.junit.Test;

import com.btr.proxy.util.ProxyException;

/*****************************************************************************
 * Unit tests for the desktop facade search strategy.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class DesktopProxySearchTest {
	
	/*************************************************************************
	 * Test method.
	 * @throws ProxyException on error.
	 ************************************************************************/
	@Test
	public void testDesktopStrategsIsWorking() throws ProxyException {
		new DesktopProxySearchStrategy().getProxySelector();
	}

}

