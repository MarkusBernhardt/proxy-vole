package com.github.markusbernhardt.proxy.search.desktop.win;

public class WinProxyConfig {

	/*****************************************************************************
	 * Proxy settings container used for the native methods. Will contain the
	 * Windows proxy settings as reported by windows WinHTTP API.
	 *
	 * @author Drew Mitchell, Copyright 2017
	 ****************************************************************************/

	private int accessType;
	private String proxy;
	private String proxyBypass;

	/*************************************************************************
	 * Constructor
	 *
	 * @param accessType
	 *            flag that specifies whether or not a proxy is in use
	 * @param proxy
	 *            the proxy server selected
	 * @param proxyBypass
	 *            the proxy bypass address list
	 ************************************************************************/

	public WinProxyConfig(int accessType, String proxy, String proxyBypass) {
		super();
		this.accessType = accessType;
		this.proxy = proxy;
		this.proxyBypass = proxyBypass;
	}

	/*************************************************************************
	 * @return Returns the access type flag.
	 ************************************************************************/

	public int getAccessType() {
		return accessType;
	}

	/*************************************************************************
	 * @return Returns the proxy.
	 ************************************************************************/

	public String getProxy() {
		return this.proxy;
	}

	/*************************************************************************
	 * @return Returns the proxyBypass.
	 ************************************************************************/

	public String getProxyBypass() {
		return this.proxyBypass;
	}

}
