package com.github.markusbernhardt.proxy.search.desktop.gnome;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.Properties;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.direct.NoProxySelector;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/*****************************************************************************
 * Loads the Gnome proxy settings from the Gnome GConf settings.
 * <p>
 * The following settings are extracted from the configuration that is stored in
 * <i>.gconf</i> folder found in the user's home directory:
 * </p>
 * <ul>
 * <li><i>org.gnome.system.proxy.http enabled</i> -&gt; bool used only by gnome-vfs
 * </li>
 * <li><i>org.gnome.system.proxy.http host</i> -&gt; string "my-proxy.example.com"
 * without "http://"</li>
 * <li><i>org.gnome.system.proxy.http port</i> -&gt; int</li>
 * <li><i>org.gnome.system.proxy.http use-authentication</i> -&gt; bool</li>
 * <li><i>org.gnome.system.proxy.http authentication-user</i> -&gt; string</li>
 * <li><i>org.gnome.system.proxy.http authentication-password</i> -&gt; string</li>
 * <li><i>org.gnome.system.proxy ignore-hosts</i> -&gt; list-of-string</li>
 * <li><i>org.gnome.system.proxy mode</i> -&gt; string THIS IS THE CANONICAL KEY;
 * SEE BELOW</li>
 * <li><i>org.gnome.system.proxy use-same-proxy</i> -&gt; bool</li>
 * <li><i>org.gnome.system.proxy.https host</i> -&gt; string
 * "proxy-for-https.example.com"</li>
 * <li><i>org.gnome.system.proxy.https port</i> -&gt; int</li>
 * <li><i>org.gnome.system.proxy.ftp host</i> -&gt; string
 * "proxy-for-ftp.example.com"</li>
 * <li><i>org.gnome.system.proxy.ftp port</i> -&gt; int</li>
 * <li><i>org.gnome.system.proxy.socks host</i> -&gt; string
 * "proxy-for-socks.example.com"</li>
 * <li><i>org.gnome.system.proxy.socks port</i> -&gt; int</li>
 * <li><i>org.gnome.system.proxy autoconfig-url</i> -&gt; string
 * "http://proxy-autoconfig.example.com"</li>
 * </ul>
 * <i>org.gnome.system.proxy mode</i> can be either:<br>
 * "none" -&gt; No proxy is used<br>
 * "manual" -&gt; The user's configuration values are used
 * (org.gnome.system.http_proxy/{host,port,etc.})<br>
 * "auto" -&gt; The "org.gnome.system.proxy/autoconfig_url" key is used <br>
 * <p>
 * GNOME Proxy_configuration settings are explained
 * <a href="https://developer.gnome.org/ProxyConfiguration/">here</a> in detail
 * </p>
 * 
 * @author Petar Vlahu Copyright 2014
 ****************************************************************************/

// TODO Work in progress: Needs unit tests and testing and code review

public class GnomeDConfProxySearchStrategy implements ProxySearchStrategy {

    /*************************************************************************
     * ProxySelector
     * 
     * @see java.net.ProxySelector#ProxySelector()
     ************************************************************************/

    public GnomeDConfProxySearchStrategy() {
        super();
    }

    /*************************************************************************
     * Loads the proxy settings and initializes a proxy selector for the Gnome
     * proxy settings.
     * 
     * @return a configured ProxySelector, null if none is found.
     * @throws ProxyException
     *             on file reading error.
     ************************************************************************/

    @Override
    public ProxySelector getProxySelector() throws ProxyException {

        Logger.log(getClass(), LogLevel.TRACE, "Detecting Gnome proxy settings");

        Properties settings = readSettings();

        String type = settings.getProperty("org.gnome.system.proxy mode");
        Logger.log(getClass(), LogLevel.TRACE, "Mode is :{0}", type);
        ProxySelector result = null;
        if (type == null) {
            String useProxy = settings.getProperty("org.gnome.system.proxy.http enabled");
            if (useProxy == null) {
                return null;
            }
            type = Boolean.parseBoolean(useProxy) ? "manual" : "none";
        }

        if ("none".equals(type)) {
            Logger.log(getClass(), LogLevel.TRACE, "Gnome uses no proxy");
            result = NoProxySelector.getInstance();
        }
        if ("manual".equals(type)) {
            Logger.log(getClass(), LogLevel.TRACE, "Gnome uses manual proxy settings");
            result = setupFixedProxySelector(settings);
        }
        if ("auto".equals(type)) {
            String pacScriptUrl = settings.getProperty("org.gnome.system.proxy autoconfig-url", "");
            Logger.log(getClass(), LogLevel.TRACE, "Gnome uses autodetect script {0}", pacScriptUrl);
            result = ProxyUtil.buildPacSelectorForUrl(pacScriptUrl);
        }

        // Wrap into white-list filter?
        String noProxyList = settings.getProperty("org.gnome.system.proxy ignore-hosts", null);
        if (result != null && noProxyList != null && noProxyList.trim().length() > 0) {
            Logger.log(getClass(), LogLevel.TRACE, "Gnome uses proxy bypass list: {0}", noProxyList);
            result = new ProxyBypassListSelector(noProxyList, result);
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
        return "gnome";
    }

    /*************************************************************************
     * Load the proxy settings from the gconf settings XML file.
     * 
     * @return the loaded settings stored in a properties object.
     * @throws ProxyException
     *             on processing error.
     ************************************************************************/

    public Properties readSettings() throws ProxyException {
        Properties settings = new Properties();
        try {
            parseSettings(settings);
        } catch (IOException e) {
            Logger.log(getClass(), LogLevel.ERROR, "Gnome settings read error.", e);
            throw new ProxyException(e);
        }
        return settings;
    }

    /*************************************************************************
     * Parse the fixed proxy settings and build an ProxySelector for this a
     * chained configuration.
     * 
     * @param settings
     *            the proxy settings to evaluate.
     ************************************************************************/

    private ProxySelector setupFixedProxySelector(Properties settings) {
        if (!hasProxySettings(settings)) {
            return null;
        }
        ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
        installHttpSelector(settings, ps);

        if (useForAllProtocols(settings)) {
            ps.setFallbackSelector(ps.getSelector("http"));
        } else {
            installSecureSelector(settings, ps);
            installFtpSelector(settings, ps);
            installSocksSelector(settings, ps);
        }
        return ps;
    }

    /*************************************************************************
     * Check if the http proxy should also be used for all other protocols.
     * 
     * @param settings
     *            to inspect.
     * @return true if only one proxy is configured else false.
     ************************************************************************/

    private boolean useForAllProtocols(Properties settings) {
        return Boolean.parseBoolean(settings.getProperty("org.gnome.system.proxy use-same-proxy", "false"));
    }

    /*************************************************************************
     * Checks if we have Proxy configuration settings in the properties.
     * 
     * @param settings
     *            to inspect.
     * @return true if we have found Proxy settings.
     ************************************************************************/

    private boolean hasProxySettings(Properties settings) {
        String proxyHost = settings.getProperty("org.gnome.system.proxy.http host", null);
        return proxyHost != null && proxyHost.length() > 0;
    }

    /*************************************************************************
     * Install a http proxy from the given settings.
     * 
     * @param settings
     *            to inspect
     * @param ps
     *            the dispatch selector to configure.
     * @throws NumberFormatException
     ************************************************************************/

    private void installHttpSelector(Properties settings, ProtocolDispatchSelector ps) throws NumberFormatException {
        String proxyHost = settings.getProperty("org.gnome.system.proxy.http host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("org.gnome.system.proxy.http port", "0").trim());
        if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
            Logger.log(getClass(), LogLevel.TRACE, "Gnome http proxy is {0}:{1}", proxyHost, proxyPort);
            ps.setSelector("http", new FixedProxySelector(proxyHost.trim(), proxyPort));
        }
    }

    /*************************************************************************
     * Install a socks proxy from the given settings.
     * 
     * @param settings
     *            to inspect
     * @param ps
     *            the dispatch selector to configure.
     * @throws NumberFormatException
     ************************************************************************/

    private void installSocksSelector(Properties settings, ProtocolDispatchSelector ps) throws NumberFormatException {
        String proxyHost = settings.getProperty("org.gnome.system.proxy.socks host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("org.gnome.system.proxy.socks port", "0").trim());
        if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
            Logger.log(getClass(), LogLevel.TRACE, "Gnome socks proxy is {0}:{1}", proxyHost, proxyPort);
            ps.setSelector("socks", new FixedProxySelector(proxyHost.trim(), proxyPort));
        }
    }

    /*************************************************************************
     * @param settings
     * @param ps
     * @throws NumberFormatException
     ************************************************************************/

    private void installFtpSelector(Properties settings, ProtocolDispatchSelector ps) throws NumberFormatException {
        String proxyHost = settings.getProperty("org.gnome.system.proxy.ftp host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("org.gnome.system.proxy.ftp port", "0").trim());
        if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
            Logger.log(getClass(), LogLevel.TRACE, "Gnome ftp proxy is {0}:{1}", proxyHost, proxyPort);
            ps.setSelector("ftp", new FixedProxySelector(proxyHost.trim(), proxyPort));
        }
    }

    /*************************************************************************
     * @param settings
     * @param ps
     * @throws NumberFormatException
     ************************************************************************/

    private void installSecureSelector(Properties settings, ProtocolDispatchSelector ps) throws NumberFormatException {
        String proxyHost = settings.getProperty("org.gnome.system.proxy.https host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("org.gnome.system.proxy.https port", "0").trim());
        if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
            Logger.log(getClass(), LogLevel.TRACE, "Gnome secure proxy is {0}:{1}", proxyHost, proxyPort);
            ps.setSelector("https", new FixedProxySelector(proxyHost.trim(), proxyPort));
            ps.setSelector("sftp", new FixedProxySelector(proxyHost.trim(), proxyPort));
        }
    }

    /*************************************************************************
     * Parse the settings file and extract all network.proxy.* settings from it.
     * 
     * @param context
     *            the gconf context to parse.
     * @param settings
     *            the settings object to fill.
     * @return the parsed properties.
     * @throws IOException
     *             on read error.
     ************************************************************************/

    private Properties parseSettings(Properties settings) throws IOException {
        String line;
        Logger.log(getClass(), LogLevel.TRACE, "exec gsettings list-recursively org.gnome.system.proxy");
        Process p = Runtime.getRuntime().exec("gsettings list-recursively org.gnome.system.proxy");
        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));

        while ((line = bri.readLine()) != null) {
            Logger.log(getClass(), LogLevel.TRACE, line);
            int schemaSep = line.indexOf(" ");
            int keySep = line.indexOf(" ", schemaSep + 1);
            String entry = line.substring(0, keySep);
            String value = line.substring(keySep + 1).replaceAll("'", "");

            // TODO 30.03.2015 bros Test for IP6 compatibility
            if (value.matches("\\[.*\\]")) {
                value = value.replaceAll("\\[|\\]| ", "");
            }

            Logger.log(getClass(), LogLevel.TRACE, "prop is: {0}|{1}", entry, value);
            settings.setProperty(entry, value);
        }
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            Logger.log(getClass(), LogLevel.ERROR, ex.getMessage());
        }
        return settings;
    }

}
