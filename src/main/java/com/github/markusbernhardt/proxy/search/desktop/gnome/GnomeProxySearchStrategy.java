package com.github.markusbernhardt.proxy.search.desktop.gnome;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.direct.NoProxySelector;
import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.EmptyXMLResolver;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Loads the Gnome proxy settings from the Gnome GConf settings.
 * <p>
 * The following settings are extracted from the configuration that is stored in
 * <i>.gconf</i> folder found in the user's home directory:
 * </p>
 * <ul>
 * <li><i>/system/http_proxy/use_http_proxy</i> -&gt; bool used only by gnome-vfs
 * </li>
 * <li><i>/system/http_proxy/host</i> -&gt; string "my-proxy.example.com" without
 * "http://"</li>
 * <li><i>/system/http_proxy/port</i> -&gt; int</li>
 * <li><i>/system/http_proxy/use_authentication</i> -&gt; bool</li>
 * <li><i>/system/http_proxy/authentication_user</i> -&gt; string</li>
 * <li><i>/system/http_proxy/authentication_password</i> -&gt; string</li>
 * <li><i>/system/http_proxy/ignore_hosts</i> -&gt; list-of-string</li>
 * <li><i>/system/proxy/mode</i> -&gt; string THIS IS THE CANONICAL KEY; SEE BELOW
 * </li>
 * <li><i>/system/proxy/secure_host</i> -&gt; string "proxy-for-https.example.com"
 * </li>
 * <li><i>/system/proxy/secure_port</i> -&gt; int</li>
 * <li><i>/system/proxy/ftp_host</i> -&gt; string "proxy-for-ftp.example.com"</li>
 * <li><i>/system/proxy/ftp_port</i> -&gt; int</li>
 * <li><i>/system/proxy/socks_host</i> -&gt; string "proxy-for-socks.example.com"
 * </li>
 * <li><i>/system/proxy/socks_port</i> -&gt; int</li>
 * <li><i>/system/proxy/autoconfig_url</i> -&gt; string
 * "http://proxy-autoconfig.example.com"</li>
 * </ul>
 * <i>/system/proxy/mode</i> can be either:<br>
 * "none" -&gt; No proxy is used<br>
 * "manual" -&gt; The user's configuration values are used
 * (/system/http_proxy/{host,port,etc.})<br>
 * "auto" -&gt; The "/system/proxy/autoconfig_url" key is used <br>
 * <p>
 * GNOME Proxy_configuration settings are explained
 * <a href="http://en.opensuse.org/GNOME/Proxy_configuration">here</a> in detail
 * </p>
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class GnomeProxySearchStrategy implements ProxySearchStrategy {

    /*************************************************************************
     * ProxySelector
     * 
     * @see java.net.ProxySelector#ProxySelector()
     ************************************************************************/

    public GnomeProxySearchStrategy() {
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

        String type = settings.getProperty("/system/proxy/mode");
        ProxySelector result = null;
        if (type == null) {
            String useProxy = settings.getProperty("/system/http_proxy/use_http_proxy");
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
            String pacScriptUrl = settings.getProperty("/system/proxy/autoconfig_url", "");
            Logger.log(getClass(), LogLevel.TRACE, "Gnome uses autodetect script {0}", pacScriptUrl);
            result = ProxyUtil.buildPacSelectorForUrl(pacScriptUrl);
        }

        // Wrap into white-list filter?
        String noProxyList = settings.getProperty("/system/http_proxy/ignore_hosts", null);
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
            parseSettings("/system/proxy/", settings);
            parseSettings("/system/http_proxy/", settings);
        } catch (IOException e) {
            Logger.log(getClass(), LogLevel.ERROR, "Gnome settings file error.", e);
            throw new ProxyException(e);
        }
        return settings;
    }

    /*************************************************************************
     * Finds the Gnome GConf settings file.
     * 
     * @param context
     *            the gconf context to parse.
     * @return a file or null if does not exist.
     ************************************************************************/

    private File findSettingsFile(String context) {
        // Normally we should inspect /etc/gconf/<version>/path to find out
        // where the actual file is.
        // But for normal systems this is always stored in .gconf folder in the
        // user's home directory.
        File userDir = new File(PlatformUtil.getUserHomeDir());

        // Build directory path for context
        StringBuilder path = new StringBuilder();
        String[] parts = context.split("/");
        for (String part : parts) {
            path.append(part);
            path.append(File.separator);
        }

        File settingsFile = new File(userDir, ".gconf" + File.separator + path.toString() + "%gconf.xml");
        if (!settingsFile.exists()) {
            Logger.log(getClass(), LogLevel.WARNING, "Gnome settings: {0} not found.", settingsFile);
            return null;
        }
        return settingsFile;
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
        return Boolean.parseBoolean(settings.getProperty("/system/http_proxy/use_same_proxy", "false"));
    }

    /*************************************************************************
     * Checks if we have Proxy configuration settings in the properties.
     * 
     * @param settings
     *            to inspect.
     * @return true if we have found Proxy settings.
     ************************************************************************/

    private boolean hasProxySettings(Properties settings) {
        String proxyHost = settings.getProperty("/system/http_proxy/host", null);
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
        String proxyHost = settings.getProperty("/system/http_proxy/host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("/system/http_proxy/port", "0").trim());
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
        String proxyHost = settings.getProperty("/system/proxy/socks_host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("/system/proxy/socks_port", "0").trim());
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
        String proxyHost = settings.getProperty("/system/proxy/ftp_host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("/system/proxy/ftp_port", "0").trim());
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
        String proxyHost = settings.getProperty("/system/proxy/secure_host", null);
        int proxyPort = Integer.parseInt(settings.getProperty("/system/proxy/secure_port", "0").trim());
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

    private Properties parseSettings(String context, Properties settings) throws IOException {

        // Read settings from file
        File settingsFile = findSettingsFile(context);
        if (settingsFile == null) {
            return settings;
        }

        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            documentBuilder.setEntityResolver(new EmptyXMLResolver());
            Document doc = documentBuilder.parse(settingsFile);
            Element root = doc.getDocumentElement();
            Node entry = root.getFirstChild();
            while (entry != null) {
                if ("entry".equals(entry.getNodeName()) && entry instanceof Element) {
                    String entryName = ((Element) entry).getAttribute("name");
                    settings.setProperty(context + entryName, getEntryValue((Element) entry));
                }
                entry = entry.getNextSibling();
            }
        } catch (SAXException e) {
            Logger.log(getClass(), LogLevel.ERROR, "Gnome settings parse error", e);
            throw new IOException(e.getMessage());
        } catch (ParserConfigurationException e) {
            Logger.log(getClass(), LogLevel.ERROR, "Gnome settings parse error", e);
            throw new IOException(e.getMessage());
        }

        return settings;
    }

    /*************************************************************************
     * Parse an entry value from a given entry node.
     * 
     * @param entry
     *            the XML node to inspect.
     * @return the value, null if it has no value.
     ************************************************************************/

    private String getEntryValue(Element entry) {
        String type = entry.getAttribute("type");

        if ("int".equals(type) || "bool".equals(type)) {
            return entry.getAttribute("value");
        }
        if ("string".equals(type)) {
            NodeList list = entry.getElementsByTagName("stringvalue");
            if (list.getLength() > 0) {
                return list.item(0).getTextContent();
            }
        }
        if ("list".equals(type)) {
            StringBuilder result = new StringBuilder();
            NodeList list = entry.getElementsByTagName("li");

            // Build comma separated list of items
            for (int i = 0; i < list.getLength(); i++) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(getEntryValue((Element) list.item(i)));
            }
            return result.toString();
        }
        return null;
    }

}
