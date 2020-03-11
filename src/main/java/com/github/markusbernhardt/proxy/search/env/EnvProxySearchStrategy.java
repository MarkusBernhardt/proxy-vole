package com.github.markusbernhardt.proxy.search.env;

import java.net.ProxySelector;
import java.util.Properties;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Reads some environment variables and extracts the proxy settings from them.
 * These variables are mainly set on linux / unix environments. The following
 * variables are read per default:
 * <ul>
 * <li><i>http_proxy</i> -&gt; This will be used for http / https</li>
 * <li><i>https_proxy</i> -&gt; Will be used for https, if not set then
 * http_proxy is used instead.</li>
 * <li><i>ftp_proxy</i> -&gt; Used for FTP.</li>
 * <li><i>no_proxy</i> -&gt; a no proxy white list.</li>
 * </ul>
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class EnvProxySearchStrategy implements ProxySearchStrategy {

	private String httpEnv;
	private String httpsEnv;
	private String ftpEnv;
	private String noProxyEnv;

	private String httpProxy;
	private String httpsProxy;
	private String ftpProxy;
	private String noProxy;

	/*************************************************************************
	 * Constructor Will use the default environment variables.
	 ************************************************************************/

	public EnvProxySearchStrategy() {
		this("http_proxy", "https_proxy", "ftp_proxy", "no_proxy");
	}

	/*************************************************************************
	 * Constructor
	 * 
	 * @param httpEnv
	 *            name of environment variable
	 * @param httpsEnv
	 *            name of environment variable
	 * @param ftpEnv
	 *            name of environment variable
	 * @param noProxyEnv
	 *            name of environment variable
	 ************************************************************************/

	public EnvProxySearchStrategy(String httpEnv, String httpsEnv, String ftpEnv, String noProxyEnv) {
		super();
		this.httpEnv = httpEnv;
		this.httpsEnv = httpsEnv;
		this.ftpEnv = ftpEnv;
		this.noProxyEnv = noProxyEnv;

		loadProxySettings();
	}

	/*************************************************************************
	 * Loads the proxy settings from the system environment variables.
	 ************************************************************************/

	private void loadProxySettings() {
		this.httpProxy = System.getenv(this.httpEnv);
		this.httpsProxy = System.getenv(this.httpsEnv);
		this.ftpProxy = System.getenv(this.ftpEnv);
		this.noProxy = System.getenv(this.noProxyEnv);
	}

	/*************************************************************************
	 * Loads the settings and stores them in a properties map.
	 * 
	 * @return the settings.
	 ************************************************************************/

	public Properties readSettings() {
		Properties result = new Properties();
		result.setProperty(this.httpEnv, this.httpProxy);
		result.setProperty(this.httpsEnv, this.httpsProxy);
		result.setProperty(this.ftpEnv, this.ftpProxy);
		result.setProperty(this.noProxyEnv, this.noProxy);
		return result;
	}

	/*************************************************************************
	 * Loads the proxy settings from environment variables.
	 * 
	 * @return a configured ProxySelector, null if none is found.
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() {

		Logger.log(getClass(), LogLevel.TRACE, "Inspecting environment variables.");

		// Check if http_proxy var is set.
		ProxySelector httpPS = ProxyUtil.parseProxySettings(this.httpProxy);
		if (httpPS == null) {
			return null;
		}

		Logger.log(getClass(), LogLevel.TRACE, "Http Proxy is {}", this.httpProxy);
		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		ps.setSelector("http", httpPS);

		ProxySelector httpsPS = ProxyUtil.parseProxySettings(this.httpsProxy);
		Logger.log(getClass(), LogLevel.TRACE, "Https Proxy is {}", httpsPS == null ? this.httpsProxy : httpsPS);
		ps.setSelector("https", httpsPS != null ? httpsPS : httpPS);

		ProxySelector ftpPS = ProxyUtil.parseProxySettings(this.ftpProxy);
		if (ftpPS != null) {
			Logger.log(getClass(), LogLevel.TRACE, "Ftp Proxy is {}", this.ftpProxy);
			ps.setSelector("ftp", ftpPS);
		}

		// Wrap with white list support
		ProxySelector result = ps;
		if (this.noProxy != null && this.noProxy.trim().length() > 0) {
			Logger.log(getClass(), LogLevel.TRACE, "Using proxy bypass list: {}", this.noProxy);
			result = new ProxyBypassListSelector(this.noProxy, ps);
		}

		return result;
	}

	/*************************************************************************
	 * Gets the printable name of the search strategy.
	 * 
	 * @return the printable name of the search strategy
	 ************************************************************************/

	@Override
	public String getName() {
		return "env";
	}

}
