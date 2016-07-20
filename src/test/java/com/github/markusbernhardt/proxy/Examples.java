package com.github.markusbernhardt.proxy;

import java.net.ProxySelector;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.ProxySearch.Strategy;
import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.github.markusbernhardt.proxy.util.PlatformUtil.Platform;

/*****************************************************************************
 * Some examples on how to use the API
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class Examples {

	public void example1() {
		ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
		ProxySelector myProxySelector = proxySearch.getProxySelector();

		ProxySelector.setDefault(myProxySelector);
	}

	public void example2() {
		ProxySearch proxySearch = new ProxySearch();

		if (PlatformUtil.getCurrentPlattform() == Platform.WIN) {
			proxySearch.addStrategy(Strategy.IE);
			proxySearch.addStrategy(Strategy.FIREFOX);
			proxySearch.addStrategy(Strategy.JAVA);
		} else if (PlatformUtil.getCurrentPlattform() == Platform.LINUX) {
			proxySearch.addStrategy(Strategy.GNOME);
			proxySearch.addStrategy(Strategy.KDE);
			proxySearch.addStrategy(Strategy.FIREFOX);
		} else {
			proxySearch.addStrategy(Strategy.OS_DEFAULT);
		}

		ProxySelector myProxySelector = proxySearch.getProxySelector();

		ProxySelector.setDefault(myProxySelector);
	}

}
