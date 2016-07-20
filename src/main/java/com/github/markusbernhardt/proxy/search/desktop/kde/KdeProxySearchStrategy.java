package com.github.markusbernhardt.proxy.search.desktop.kde;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.Properties;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.env.EnvProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.wpad.WpadProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.direct.NoProxySelector;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.UseProxyWhiteListSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;

/*****************************************************************************
 * Loads the KDE4 proxy settings from the KDE <i>kioslaverc</i> file. This will
 * load properties from the file
 * <p>
 * <i>.kde/share/config/kioslaverc</i>
 * </P>
 * starting from the current users home directory.
 * <p>
 * The following settings are extracted from the section "[Proxy Settings]":
 * </p>
 * <ul>
 * <li><i>AuthMode</i> -&gt; 0 = no auth., 1 = use login.</li>
 * <li><i>ProxyType</i> -&gt; 0 = direct 1 = use fixed settings, 2 = use PAC, 3
 * = automatic (WPAD) , 4 = Use environment variables?</li>
 * <li><i>Proxy Config</i> Script -&gt; URL to PAC file</li>
 * <li><i>ftpProxy</i> -&gt; Fixed ftp proxy address e.g.
 * http://www.ftp-proxy.com:8080</li>
 * <li><i>httpProxy</i> -&gt; Fixed http proxy e.g
 * http://www.http-proxy.com:8080</li>
 * <li><i>httpsProxy</i> -&gt; Fixed https proxy e.g
 * http://www.https-proxy.com:8080</li>
 * <li><i>NoProxyFor</i> -&gt; Proxy white list</li>
 * <li><i>ReversedException</i> -&gt; false = use NoProxyFor, true = revert
 * meaning of the NoProxyFor list</li>
 * </ul>
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class KdeProxySearchStrategy implements ProxySearchStrategy {

	private KdeSettingsParser settingsParser;

	/*************************************************************************
	 * ProxySelector using the given parser.
	 * 
	 * @see java.net.ProxySelector#ProxySelector()
	 ************************************************************************/

	public KdeProxySearchStrategy() {
		this(new KdeSettingsParser());
	}

	/*************************************************************************
	 * ProxySelector
	 * 
	 * @param settingsParser
	 *            the KdeSettingsParser instance to use.
	 * @see java.net.ProxySelector#ProxySelector()
	 ************************************************************************/

	public KdeProxySearchStrategy(KdeSettingsParser settingsParser) {
		super();
		this.settingsParser = settingsParser;
	}

	/*************************************************************************
	 * Loads the proxy settings and initializes a proxy selector for the firefox
	 * proxy settings.
	 * 
	 * @return a configured ProxySelector, null if none is found.
	 * @throws ProxyException
	 *             on file reading error.
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() throws ProxyException {

		Logger.log(getClass(), LogLevel.TRACE, "Detecting Kde proxy settings");

		Properties settings = readSettings();
		if (settings == null) {
			return null;
		}

		ProxySelector result = null;
		int type = Integer.parseInt(settings.getProperty("ProxyType", "-1"));
		switch (type) {
		case 0: // Use no proxy
			Logger.log(getClass(), LogLevel.TRACE, "Kde uses no proxy");
			result = NoProxySelector.getInstance();
			break;
		case 1: // Fixed settings
			Logger.log(getClass(), LogLevel.TRACE, "Kde uses manual proxy settings");
			result = setupFixedProxySelector(settings);
			break;
		case 2: // PAC Script
			String pacScriptUrl = settings.getProperty("Proxy Config Script", "");
			Logger.log(getClass(), LogLevel.TRACE, "Kde uses autodetect script {0}", pacScriptUrl);
			result = ProxyUtil.buildPacSelectorForUrl(pacScriptUrl);
			break;
		case 3: // WPAD
			Logger.log(getClass(), LogLevel.TRACE, "Kde uses WPAD to detect the proxy");
			result = new WpadProxySearchStrategy().getProxySelector();
			break;
		case 4: // Use environment variables
			Logger.log(getClass(), LogLevel.TRACE, "Kde reads proxy from environment");
			result = setupEnvVarSelector(settings);
			break;
		default:
			break;
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
		return "kde";
	}

	/*************************************************************************
	 * Reads the settings and stores them in a properties map.
	 * 
	 * @return the parsed settings.
	 * @throws ProxyException
	 ************************************************************************/

	private Properties readSettings() throws ProxyException {
		try {
			return this.settingsParser.parseSettings();
		} catch (IOException e) {
			Logger.log(getClass(), LogLevel.ERROR, "Can't parse settings.", e);
			throw new ProxyException(e);
		}
	}

	/*************************************************************************
	 * Builds an environment variable selector.
	 * 
	 * @param settings
	 *            the settings to read from.
	 * @return the ProxySelector using environment variables.
	 ************************************************************************/

	private ProxySelector setupEnvVarSelector(Properties settings) {
		ProxySelector result;
		result = new EnvProxySearchStrategy(settings.getProperty("httpProxy"), settings.getProperty("httpsProxy"),
		        settings.getProperty("ftpProxy"), settings.getProperty("NoProxyFor")).getProxySelector();
		return result;
	}

	/*************************************************************************
	 * Parse the fixed proxy settings and build an ProxySelector for this a
	 * chained configuration.
	 * 
	 * @param settings
	 *            the proxy settings to evaluate.
	 ************************************************************************/

	private ProxySelector setupFixedProxySelector(Properties settings) {
		String proxyVar = settings.getProperty("httpProxy", null);
		FixedProxySelector httpPS = ProxyUtil.parseProxySettings(proxyVar);
		if (httpPS == null) {
			Logger.log(getClass(), LogLevel.TRACE, "Kde http proxy is {0}", proxyVar);
			return null;
		}

		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		ps.setSelector("http", httpPS);

		proxyVar = settings.getProperty("httpsProxy", null);
		FixedProxySelector httpsPS = ProxyUtil.parseProxySettings(proxyVar);
		if (httpsPS != null) {
			Logger.log(getClass(), LogLevel.TRACE, "Kde https proxy is {0}", proxyVar);
			ps.setSelector("https", httpsPS);
		}

		proxyVar = settings.getProperty("ftpProxy", null);
		FixedProxySelector ftpPS = ProxyUtil.parseProxySettings(proxyVar);
		if (ftpPS != null) {
			Logger.log(getClass(), LogLevel.TRACE, "Kde ftp proxy is {0}", proxyVar);
			ps.setSelector("ftp", ftpPS);
		}

		// Wrap in white list filter.
		String noProxyList = settings.getProperty("NoProxyFor", null);
		if (noProxyList != null && noProxyList.trim().length() > 0) {
			boolean reverse = "true".equals(settings.getProperty("ReversedException", "false"));
			if (reverse) {
				Logger.log(getClass(), LogLevel.TRACE, "Kde proxy blacklist is {0}", noProxyList);
				return new UseProxyWhiteListSelector(noProxyList, ps);
			} else {
				Logger.log(getClass(), LogLevel.TRACE, "Kde proxy whitelist is {0}", noProxyList);
				return new ProxyBypassListSelector(noProxyList, ps);
			}
		}

		return ps;
	}

}
