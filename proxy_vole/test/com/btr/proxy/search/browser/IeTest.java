package com.btr.proxy.search.browser;

import org.junit.Test;

import com.btr.proxy.search.browser.ie.IEProxySearchStrategy;
import com.btr.proxy.util.PlatformUtil;
import com.btr.proxy.util.ProxyException;
import com.btr.proxy.util.PlatformUtil.Platform;

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

}

