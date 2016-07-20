package com.github.markusbernhardt.proxy.search.browser.ie;

import java.net.URI;

import com.github.markusbernhardt.proxy.util.UriFilter;

/*****************************************************************************
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class IELocalByPassFilter implements UriFilter {

	/*************************************************************************
	 * accept
	 * 
	 * @see com.github.markusbernhardt.proxy.util.UriFilter#accept(java.net.URI)
	 ************************************************************************/

	public boolean accept(URI uri) {
		if (uri == null) {
			return false;
		}
		String host = uri.getAuthority();
		return host != null && !host.contains(".");
	}

}
